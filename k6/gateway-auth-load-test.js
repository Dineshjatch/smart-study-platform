import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:30080';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 30 },
        { duration: '30s', target: 0 },
    ],
};

export default function () {
    const response = http.get(`${BASE_URL}/api/quizzes`, {
        headers: {
            Authorization: `Bearer ${__ENV.JWT_TOKEN}`,
        },
    });

    check(response, {
        'authenticated request successful': (r) => r.status === 200,
    });

    sleep(0.2);
}