interface CacheItem<T> {
    data: T;
    timestamp: number;
    expiresIn: number;
}

class Cache {
    private static instance: Cache;
    private cache: Map<string, CacheItem<any>>;
    private readonly DEFAULT_EXPIRY = 5 * 60 * 1000; // 5분

    private constructor() {
        this.cache = new Map();
    }

    static getInstance(): Cache {
        if (!Cache.instance) {
            Cache.instance = new Cache();
        }
        return Cache.instance;
    }

    set<T>(key: string, data: T, expiresIn: number = this.DEFAULT_EXPIRY): void {
        this.cache.set(key, {
            data,
            timestamp: Date.now(),
            expiresIn
        });
    }

    get<T>(key: string): T | null {
        const item = this.cache.get(key);
        if (!item) return null;

        if (Date.now() - item.timestamp > item.expiresIn) {
            this.cache.delete(key);
            return null;
        }

        return item.data as T;
    }

    delete(key: string): void {
        this.cache.delete(key);
    }

    clear(): void {
        this.cache.clear();
    }
}

export const cache = Cache.getInstance();

export const withCache = async <T>(
    key: string,
    fetchFn: () => Promise<T>,
    expiresIn?: number
): Promise<T> => {
    const cachedData = cache.get<T>(key);
    if (cachedData) return cachedData;

    const data = await fetchFn();
    cache.set(key, data, expiresIn);
    return data;
}; 