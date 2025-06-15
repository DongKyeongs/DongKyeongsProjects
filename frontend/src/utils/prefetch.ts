interface PrefetchConfig {
    priority: 'high' | 'low';
    timeout: number;
}

const defaultConfig: PrefetchConfig = {
    priority: 'low',
    timeout: 5000
};

class PrefetchManager {
    private static instance: PrefetchManager;
    private queue: Map<string, Promise<any>>;
    private config: PrefetchConfig;

    private constructor() {
        this.queue = new Map();
        this.config = defaultConfig;
    }

    static getInstance(): PrefetchManager {
        if (!PrefetchManager.instance) {
            PrefetchManager.instance = new PrefetchManager();
        }
        return PrefetchManager.instance;
    }

    setConfig(config: Partial<PrefetchConfig>) {
        this.config = { ...this.config, ...config };
    }

    async prefetch<T>(
        key: string,
        fetchFn: () => Promise<T>,
        config: Partial<PrefetchConfig> = {}
    ): Promise<void> {
        const { priority, timeout } = { ...this.config, ...config };

        if (this.queue.has(key)) {
            return;
        }

        const promise = new Promise<void>((resolve) => {
            if (priority === 'low') {
                setTimeout(() => {
                    fetchFn().finally(() => {
                        this.queue.delete(key);
                        resolve();
                    });
                }, 0);
            } else {
                fetchFn().finally(() => {
                    this.queue.delete(key);
                    resolve();
                });
            }
        });

        this.queue.set(key, promise);

        if (timeout > 0) {
            setTimeout(() => {
                if (this.queue.has(key)) {
                    this.queue.delete(key);
                }
            }, timeout);
        }
    }

    async waitForPrefetch(key: string): Promise<void> {
        const promise = this.queue.get(key);
        if (promise) {
            await promise;
        }
    }

    clear() {
        this.queue.clear();
    }
}

export const prefetchManager = PrefetchManager.getInstance();

export const prefetch = async <T>(
    key: string,
    fetchFn: () => Promise<T>,
    config: Partial<PrefetchConfig> = {}
): Promise<void> => {
    return prefetchManager.prefetch(key, fetchFn, config);
}; 