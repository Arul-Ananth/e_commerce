import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import ApiService, {
    fetchCategories,
    fetchProduct,
    fetchProducts,
    fetchReviews,
    getAllUsers,
} from "./ApiService";

describe("ApiService", () => {
    let mock: MockAdapter;

    beforeEach(() => {
        mock = new MockAdapter(ApiService.api);
        ApiService.setUnauthorizedHandler(null);
    });

    afterEach(() => {
        mock.restore();
        vi.restoreAllMocks();
    });

    it("normalizes category responses and uses the Axios api client", async () => {
        mock.onGet("/products/categories").reply(200, ["Books", 5, "Electronics"]);

        await expect(fetchCategories()).resolves.toEqual(["All", "Books", "Electronics"]);
        expect(mock.history.get[0].url).toBe("/products/categories");
    });

    it("requests product lists with expected params", async () => {
        mock.onGet("/products").reply((config) => {
            expect(config.params).toEqual({ page: 0, size: 100, category: "Books" });
            return [200, { items: [{ id: 1, name: "Book" }], hasNext: false }];
        });

        await expect(fetchProducts("Books")).resolves.toEqual([{ id: 1, name: "Book" }]);
    });

    it("returns null for missing product details", async () => {
        vi.spyOn(console, "error").mockImplementation(() => undefined);
        mock.onGet("/products/404").reply(404, { message: "Product not found" });

        await expect(fetchProduct(404)).resolves.toBeNull();
    });

    it("normalizes paged reviews", async () => {
        mock.onGet("/products/1/reviews").reply(200, { items: [{ id: 2, user: "reader", rating: 5, comment: "Good" }] });

        await expect(fetchReviews(1)).resolves.toEqual([{ id: 2, user: "reader", rating: 5, comment: "Good" }]);
    });

    it("requests users with backend-compatible page size", async () => {
        mock.onGet("/users").reply((config) => {
            expect(config.params).toEqual({ page: 0, size: 100 });
            return [200, { items: [{ id: 1, email: "admin@example.com", roles: ["ROLE_ADMIN"] }] }];
        });

        await expect(getAllUsers()).resolves.toEqual([{ id: 1, email: "admin@example.com", roles: ["ROLE_ADMIN"] }]);
    });

    it("runs the unauthorized handler through the Axios interceptor", async () => {
        const handler = vi.fn();
        ApiService.setUnauthorizedHandler(handler);
        mock.onGet("/products/1").reply(401, { message: "Unauthorized" });
        vi.spyOn(console, "error").mockImplementation(() => undefined);

        await expect(fetchProduct(1)).resolves.toBeNull();
        expect(handler).toHaveBeenCalledOnce();
    });
});
