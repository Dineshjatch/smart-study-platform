FROM nginx:alpine

# Remove nginx's default web files.
RUN rm -rf /usr/share/nginx/html/*

# Copy our frontend into nginx's web root.
COPY index.html /usr/share/nginx/html/
COPY css /usr/share/nginx/html/css
COPY js /usr/share/nginx/html/js

# Use our nginx configuration.
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80