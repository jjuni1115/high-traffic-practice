import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    vus: 500,             // 동시에 500명 요청
    duration: '10s',      // 10초간 지속
};

export default function () {
    const userId = Math.floor(Math.random() * 100000);
    const res = http.post(`http://localhost:8080/coupon/issue/3/${userId}`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}