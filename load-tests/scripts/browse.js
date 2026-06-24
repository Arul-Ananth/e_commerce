import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { BASE_URL, randomItem } from '../lib/config.js';

const products = new SharedArray('products', () => JSON.parse(open('../data/products.json')));

export const options = {
  scenarios: {
    browse: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.BROWSE_RAMP_UP || '30s', target: Number(__ENV.BROWSE_VUS || 10) },
        { duration: __ENV.BROWSE_DURATION || '2m', target: Number(__ENV.BROWSE_VUS || 10) },
        { duration: __ENV.BROWSE_RAMP_DOWN || '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{scenario:browse}': ['p(95)<500'],
  },
};

export default function () {
  const product = randomItem(products);

  const list = http.get(`${BASE_URL}/api/v1/products?page=0&size=20`);
  check(list, {
    'product list status is 200': (res) => res.status === 200,
  });

  const categoryList = http.get(
    `${BASE_URL}/api/v1/products?category=${encodeURIComponent(product.category)}&page=0&size=20`
  );
  check(categoryList, {
    'category product list status is 200': (res) => res.status === 200,
  });

  const detail = http.get(`${BASE_URL}/api/v1/products/${product.id}`);
  check(detail, {
    'product detail status is 200': (res) => res.status === 200,
  });

  const reviews = http.get(`${BASE_URL}/api/v1/products/${product.id}/reviews?page=0&size=20`);
  check(reviews, {
    'reviews status is 200': (res) => res.status === 200,
  });

  if (Math.random() < 0.2) {
    const categories = http.get(`${BASE_URL}/api/v1/products/categories`);
    check(categories, {
      'categories status is 200': (res) => res.status === 200,
    });
  }

  sleep(Number(__ENV.BROWSE_SLEEP_SECONDS || 1));
}
