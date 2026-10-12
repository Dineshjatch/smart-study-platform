terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0, < 7.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type for the Smart Study Platform"
  type        = string
  default     = "t3.medium"
}

data "aws_caller_identity" "current" {}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

locals {
  asset_bucket_name = "smart-study-assets-${data.aws_caller_identity.current.account_id}-${var.aws_region}"
}

resource "aws_s3_bucket" "assets" {
  bucket        = local.asset_bucket_name
  force_destroy = true

  tags = {
    Name    = "smart-study-assets"
    Project = "SmartStudyPlatform"
  }
}

resource "aws_s3_bucket_public_access_block" "assets" {
  bucket                  = aws_s3_bucket.assets.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "assets" {
  bucket = aws_s3_bucket.assets.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_iam_role" "ec2" {
  name = "smart-study-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "assets_read" {
  name = "smart-study-read-assets"
  role = aws_iam_role.ec2.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["s3:ListBucket"]
        Resource = aws_s3_bucket.assets.arn
      },
      {
        Effect   = "Allow"
        Action   = ["s3:GetObject"]
        Resource = "${aws_s3_bucket.assets.arn}/*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "smart-study-ec2-profile"
  role = aws_iam_role.ec2.name
}

resource "aws_security_group" "app" {
  name        = "smart-study-web-sg"
  description = "Public HTTP access to the Smart Study frontend"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP frontend"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow outbound connections for package and image downloads"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "smart-study-web-sg"
    Project = "SmartStudyPlatform"
  }
}

resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.al2023_ami.value
  instance_type               = var.instance_type
  subnet_id                   = data.aws_subnets.default.ids[0]
  vpc_security_group_ids      = [aws_security_group.app.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  associate_public_ip_address = true

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    volume_size           = 20
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  user_data = <<-EOF
    #!/bin/bash
    set -euxo pipefail

    dnf install -y docker
    systemctl enable --now docker

    mkdir -p /usr/local/lib/docker/cli-plugins
    curl --retry 5 --retry-delay 2 -fsSL \
      https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    usermod -aG docker ec2-user || true
    mkdir -p /opt/smart-study
    chmod 755 /opt/smart-study

    systemctl enable --now amazon-ssm-agent
  EOF

  depends_on = [
    aws_iam_role_policy_attachment.ssm,
    aws_iam_role_policy.assets_read
  ]

  tags = {
    Name    = "smart-study-app-server"
    Project = "SmartStudyPlatform"
  }
}

output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "public_ip" {
  description = "Public IP of the EC2 instance"
  value       = aws_eip.app.public_ip
}

output "application_url" {
  description = "HTTP URL of the application"
  value       = "http://${aws_eip.app.public_ip}"
}

output "s3_bucket_name" {
  description = "Private S3 bucket for deployment files"
  value       = aws_s3_bucket.assets.bucket
}

output "region" {
  value = var.aws_region
}
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name    = "smart-study-app-eip"
    Project = "SmartStudyPlatform"
  }
}
resource "aws_ssm_document" "smart_study_deploy" {
  name            = "SmartStudyDeploy"
  document_type   = "Command"
  document_format = "JSON"

  content = jsonencode({
    schemaVersion = "2.2"
    description   = "Deploy a numeric Smart Study Docker image tag to the existing EC2 instance."

    parameters = {
      ImageTag = {
        type           = "String"
        description    = "Numeric Jenkins build number to deploy"
        allowedPattern = "^[0-9]+$"
      }
    }

    mainSteps = [
      {
        action = "aws:runShellScript"
        name   = "DeploySmartStudy"

        inputs = {
          runCommand = [
            "set -euo pipefail",
            "cd /opt/smart-study",
            "sed -i 's/^IMAGE_TAG=.*/IMAGE_TAG={{ ImageTag }}/' .env.aws",
            "docker compose --env-file .env.aws -f docker-compose.aws.yml pull",
            "docker compose --env-file .env.aws -f docker-compose.aws.yml up -d",
            "docker compose --env-file .env.aws -f docker-compose.aws.yml ps"
          ]
        }
      }
    ]
  })
}

resource "aws_iam_user" "jenkins_deploy" {
  name = "smart-study-jenkins-deploy"

  tags = {
    Project = "SmartStudyPlatform"
    Purpose = "Jenkins deployment to existing EC2 instance"
  }
}

resource "aws_iam_user_policy" "jenkins_deploy" {
  name = "smart-study-ssm-deploy-only"
  user = aws_iam_user.jenkins_deploy.name

  policy = jsonencode({
    Version = "2012-10-17"

    Statement = [
      {
        Sid    = "SendOnlyApprovedDeploymentDocument"
        Effect = "Allow"
        Action = [
          "ssm:SendCommand"
        ]
        Resource = [
          aws_ssm_document.smart_study_deploy.arn,
          aws_instance.app.arn
        ]
      },
      {
        Sid      = "ReadCommandResult"
        Effect   = "Allow"
        Action   = ["ssm:GetCommandInvocation"]
        Resource = "*"
      }
    ]
  })
}