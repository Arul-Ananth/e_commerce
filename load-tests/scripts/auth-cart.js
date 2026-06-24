import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { BASE_URL, jsonHeaders, parseCsv, randomItem } from '../lib/config.js';
import { ensureUser, login } from '../lib/auth.js';

const users = new SharedArray('users', () => parseCsv(open('../data/users.csv')));
const products = new SharedArray('products', () => JSON.parse(open('../data/products.json')));

export const options = {
  scenarios: {
    cart: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.CART_RAMP_UP || '30s', target: Number(__ENV.CART_VUS || 10) },
        { duration: __ENV.CART_DURATION || '2m', target: Number(__ENV.CART_VUS || 10) },
        { duration: __ENV.CART_RAMP_DOWN || '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{scenario:cart}': ['p(95)<800'],
  },
};

export function setup() {
  const requestedVus = Number(__ENV.CART_VUS || 10);
  if (requestedVus > users.length) {
    fail(`CART_VUS=${requestedVus} exceeds users.csv count=${users.length}. Add users or lower CART_VUS.`);
  }

  return {
    tokens: users.map((user) => {
      ensureUser(user);
      return login(user);
    }),
  };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const product = randomItem(products);
  const params = jsonHeaders(token);

  const cart = http.get(`${BASE_URL}/api/v1/cart`, params);
  check(cart, {
    'cart read status is 200': (res) => res.status === 200,
  });

  const add = http.post(
    `${BASE_URL}/api/v1/cart/items`,
    JSON.stringify({ productId: product.id, quantity: 1, discountId: null }),
    params
  );
  check(add, {
    'cart add status is 200': (res) => res.status === 200,
  });

  const update = http.patch(
    `${BASE_URL}/api/v1/cart/items/${product.id}`,
    JSON.stringify({ quantity: 2 }),
    params
  );
  check(update, {
    'cart quantity update status is 200': (res) => res.status === 200,
  });

  const remove = http.del(`${BASE_URL}/api/v1/cart/items/${product.id}`, null, params);
  check(remove, {
    'cart remove status is 200': (res) => res.status === 200,
  });

  sleep(Number(__ENV.CART_SLEEP_SECONDS || 1));
}
