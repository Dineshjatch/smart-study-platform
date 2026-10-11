import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
    ],
};

export default function () {
    const response = http.get(
        'http://localhost:38080/api/quizzes'
    );

    check(response, {
        'request completed': (r) =>
            r.status === 200 || r.status === 401,
    });

    sleep(1);
}