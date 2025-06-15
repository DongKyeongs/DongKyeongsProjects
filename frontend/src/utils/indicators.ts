import { CandlestickData, Time } from 'lightweight-charts';

// 이동평균선 (MA) 계산
export const calculateMA = (data: CandlestickData[], period: number): number[] => {
    const ma: number[] = [];
    for (let i = 0; i < data.length; i++) {
        if (i < period - 1) {
            ma.push(NaN);
            continue;
        }
        const sum = data.slice(i - period + 1, i + 1).reduce((acc, curr) => acc + curr.close, 0);
        ma.push(sum / period);
    }
    return ma;
};

// RSI 계산
export const calculateRSI = (data: CandlestickData[], period: number = 14): number[] => {
    const rsi: number[] = [];
    let gains = 0;
    let losses = 0;

    // 초기 평균 이득/손실 계산
    for (let i = 1; i < period + 1; i++) {
        const change = data[i].close - data[i - 1].close;
        if (change >= 0) {
            gains += change;
        } else {
            losses -= change;
        }
    }

    let avgGain = gains / period;
    let avgLoss = losses / period;

    // 첫 번째 RSI 값 계산
    let rs = avgGain / avgLoss;
    rsi.push(100 - (100 / (1 + rs)));

    // 나머지 RSI 값 계산
    for (let i = period + 1; i < data.length; i++) {
        const change = data[i].close - data[i - 1].close;
        avgGain = ((avgGain * (period - 1)) + (change > 0 ? change : 0)) / period;
        avgLoss = ((avgLoss * (period - 1)) + (change < 0 ? -change : 0)) / period;
        rs = avgGain / avgLoss;
        rsi.push(100 - (100 / (1 + rs)));
    }

    return rsi;
};

// MACD 계산
export const calculateMACD = (data: CandlestickData[]): { macd: number[], signal: number[], histogram: number[] } => {
    const ema12 = calculateEMA(data, 12);
    const ema26 = calculateEMA(data, 26);
    const macd: number[] = [];
    const signal: number[] = [];
    const histogram: number[] = [];

    // MACD 라인 계산
    for (let i = 0; i < data.length; i++) {
        macd.push(ema12[i] - ema26[i]);
    }

    // 시그널 라인 계산 (MACD의 9일 EMA)
    const signalLine = calculateEMA(macd.map((value, index) => ({
        time: data[index].time,
        close: value
    })), 9);

    // 히스토그램 계산
    for (let i = 0; i < macd.length; i++) {
        signal.push(signalLine[i]);
        histogram.push(macd[i] - signalLine[i]);
    }

    return { macd, signal, histogram };
};

// EMA 계산
const calculateEMA = (data: { time: Time, close: number }[], period: number): number[] => {
    const k = 2 / (period + 1);
    const ema: number[] = [];
    let sum = 0;

    // 첫 번째 EMA는 SMA로 계산
    for (let i = 0; i < period; i++) {
        sum += data[i].close;
    }
    ema.push(sum / period);

    // 나머지 EMA 계산
    for (let i = period; i < data.length; i++) {
        ema.push(data[i].close * k + ema[ema.length - 1] * (1 - k));
    }

    return ema;
};

// 볼린저 밴드 계산
export const calculateBollingerBands = (data: CandlestickData[], period: number = 20, multiplier: number = 2): {
    upper: number[],
    middle: number[],
    lower: number[]
} => {
    const middle = calculateMA(data, period);
    const upper: number[] = [];
    const lower: number[] = [];

    for (let i = 0; i < data.length; i++) {
        if (i < period - 1) {
            upper.push(NaN);
            lower.push(NaN);
            continue;
        }

        const slice = data.slice(i - period + 1, i + 1);
        const standardDeviation = Math.sqrt(
            slice.reduce((sum, curr) => sum + Math.pow(curr.close - middle[i], 2), 0) / period
        );

        upper.push(middle[i] + (standardDeviation * multiplier));
        lower.push(middle[i] - (standardDeviation * multiplier));
    }

    return { upper, middle, lower };
}; 