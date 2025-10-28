import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800']
    },
    stages: [
        { duration: '30s', target: 10 },
        { duration: '2m',  target: 50 },
        { duration: '30s', target: 0 },
    ],
};

const URL = 'http://localhost:8080/get-article-info-by-doi';
const headers = { 'Content-Type': 'application/json' };

const body = JSON.stringify({
    dois: [
        '10.1038/s41586-020-2649-2',
        '10.1109/5.771073',
        '10.1145/3292500.3330701',
        '10.1093/nar/gkaa1104',
        '10.1093/bioinformatics/btz575'
    ]
});

export default function () {
    const res = http.post(URL, body, { headers });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'is json':      (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
        'has items':    (r) => JSON.parse(r.body).length >= 1,
    });

    sleep(1);
}
