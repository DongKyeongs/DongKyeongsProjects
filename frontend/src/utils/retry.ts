interface RetryConfig {
    maxAttempts: number;
    delay: number;
    backoff: number;
}

const defaultConfig: RetryConfig = {
    maxAttempts: 3,
    delay: 1000,
    backoff: 2
};

export const withRetry = async <T>(
    fn: () => Promise<T>,
    config: Partial<RetryConfig> = {}
): Promise<T> => {
    const { maxAttempts, delay, backoff } = { ...defaultConfig, ...config };
    let lastError: Error | null = null;

    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            return await fn();
        } catch (error) {
            lastError = error as Error;
            
            if (attempt === maxAttempts) {
                throw new Error(`최대 재시도 횟수(${maxAttempts})를 초과했습니다: ${lastError.message}`);
            }

            const waitTime = delay * Math.pow(backoff, attempt - 1);
            await new Promise(resolve => setTimeout(resolve, waitTime));
        }
    }

    throw lastError;
};

export const isRetryableError = (error: any): boolean => {
    if (!error.response) return true;
    
    const status = error.response.status;
    return (
        status === 408 || // Request Timeout
        status === 429 || // Too Many Requests
        status === 500 || // Internal Server Error
        status === 502 || // Bad Gateway
        status === 503 || // Service Unavailable
        status === 504    // Gateway Timeout
    );
}; 