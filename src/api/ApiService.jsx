// services/apiService.js

const API_BASE = 'http://localhost:8080/product';

export async function fetchCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        const data = await res.json();
        return ['All', ...data];
    } catch (err) {
        console.error('Error fetching categories:', err);
        return [];
    }
}


export async function fetchProducts(category) {
    try {
        const url = category === 'All'
            ? `${API_BASE}/list`
            : `${API_BASE}/category/${category.toLowerCase()}`;

        const res = await fetch(url);
        return await res.json();
    } catch (err) {
        console.error('Error fetching products:', err);
        return [];
    }
}

export async function fetchProduct(id) {
    try{
        const res = await fetch(`${API_BASE}/${id}`);
        return await res.json();
    }
    catch(err){
        console.error('Error fetching product:', err);
        return {};
    }
}

export async function fetchReviews(id) {
    try{
        const res = await fetch(`${API_BASE}/reviews/${id}`);
        return await res.json();
    }
    catch(err){
        console.error('Error fetching reviews:', err);
        return [];
    }
}
