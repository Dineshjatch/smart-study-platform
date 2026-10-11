import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://api-gateway:8080';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 30 },
        { duration: '30s', target: 0 },
    ],
};

export function setup() {
    const response = http.post(
        `${BASE_URL}/api/users/login`,
        JSON.stringify({
            email: __ENV.TEST_EMAIL,
            password: __ENV.TEST_PASSWORD,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(response, {
        'login successful': (r) => r.status === 200,
    });

    if (response.status !== 200) {
        throw new Error(`Login failed with status ${response.status}`);
    }

    return {
        token: response.json().token,
    };
}

export default function (data) {
    const response = http.get(`${BASE_URL}/api/quizzes`, {
        headers: {
            Authorization: `Bearer ${data.token}`,
        },
    });

    check(response, {
        'authenticated request successful': (r) => r.status === 200,
    });

    sleep(0.2);
}