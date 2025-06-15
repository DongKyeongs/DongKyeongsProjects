import { CandlestickData } from 'lightweight-charts';

interface ExtendedCandlestickData extends CandlestickData {
    volume?: number;
}

export const exportToCSV = (data: ExtendedCandlestickData[], symbol: string) => {
    const headers = ['시간', '시가', '고가', '저가', '종가', '거래량'];
    const csvContent = [
        headers.join(','),
        ...data.map(candle => [
            new Date(candle.time as number).toISOString(),
            candle.open,
            candle.high,
            candle.low,
            candle.close,
            candle.volume
        ].join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `${symbol}_chart_data.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

export const exportToImage = (chartElement: HTMLElement, symbol: string) => {
    const canvas = chartElement.querySelector('canvas');
    if (!canvas) return;

    const link = document.createElement('a');
    link.download = `${symbol}_chart.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
};

export const exportChartSettings = (settings: any) => {
    const settingsStr = JSON.stringify(settings, null, 2);
    const blob = new Blob([settingsStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = 'chart_settings.json';
    link.click();
    
    URL.revokeObjectURL(url);
};

export const importChartSettings = (file: File): Promise<any> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        
        reader.onload = (event) => {
            try {
                const settings = JSON.parse(event.target?.result as string);
                resolve(settings);
            } catch (error) {
                reject(new Error('설정 파일 형식이 올바르지 않습니다.'));
            }
        };
        
        reader.onerror = () => {
            reject(new Error('파일을 읽는 중 오류가 발생했습니다.'));
        };
        
        reader.readAsText(file);
    });
}; 