import React, { useState } from 'react';
import './ChartSettings.css';

interface ChartSettingsProps {
    onSettingsChange: (settings: ChartSettings) => void;
    initialSettings: ChartSettings;
}

export interface ChartSettings {
    theme: 'light' | 'dark';
    chartStyle: {
        upColor: string;
        downColor: string;
        borderVisible: boolean;
        wickUpColor: string;
        wickDownColor: string;
    };
    indicators: {
        ma: {
            enabled: boolean;
            period: number;
            color: string;
        };
        rsi: {
            enabled: boolean;
            period: number;
            overbought: number;
            oversold: number;
        };
        macd: {
            enabled: boolean;
            fastPeriod: number;
            slowPeriod: number;
            signalPeriod: number;
        };
        bollinger: {
            enabled: boolean;
            period: number;
            multiplier: number;
        };
    };
}

const ChartSettings: React.FC<ChartSettingsProps> = ({ onSettingsChange, initialSettings }) => {
    const [settings, setSettings] = useState<ChartSettings>(initialSettings);
    const [isOpen, setIsOpen] = useState(false);

    const handleChange = (key: string, value: any) => {
        const newSettings = {
            ...settings,
            [key]: value
        };
        setSettings(newSettings);
        onSettingsChange(newSettings);
    };

    const handleIndicatorChange = (indicator: string, key: string, value: any) => {
        const newSettings = {
            ...settings,
            indicators: {
                ...settings.indicators,
                [indicator]: {
                    ...settings.indicators[indicator as keyof typeof settings.indicators],
                    [key]: value
                }
            }
        };
        setSettings(newSettings);
        onSettingsChange(newSettings);
    };

    return (
        <div className="chart-settings">
            <button 
                className="settings-toggle"
                onClick={() => setIsOpen(!isOpen)}
            >
                설정
            </button>

            {isOpen && (
                <div className="settings-panel">
                    <div className="settings-section">
                        <h3>테마</h3>
                        <select
                            value={settings.theme}
                            onChange={(e) => handleChange('theme', e.target.value)}
                        >
                            <option value="light">라이트 모드</option>
                            <option value="dark">다크 모드</option>
                        </select>
                    </div>

                    <div className="settings-section">
                        <h3>차트 스타일</h3>
                        <div className="color-picker">
                            <label>
                                상승 색상:
                                <input
                                    type="color"
                                    value={settings.chartStyle.upColor}
                                    onChange={(e) => handleChange('chartStyle', {
                                        ...settings.chartStyle,
                                        upColor: e.target.value
                                    })}
                                />
                            </label>
                            <label>
                                하락 색상:
                                <input
                                    type="color"
                                    value={settings.chartStyle.downColor}
                                    onChange={(e) => handleChange('chartStyle', {
                                        ...settings.chartStyle,
                                        downColor: e.target.value
                                    })}
                                />
                            </label>
                        </div>
                    </div>

                    <div className="settings-section">
                        <h3>이동평균선 (MA)</h3>
                        <label>
                            <input
                                type="checkbox"
                                checked={settings.indicators.ma.enabled}
                                onChange={(e) => handleIndicatorChange('ma', 'enabled', e.target.checked)}
                            />
                            활성화
                        </label>
                        <input
                            type="number"
                            value={settings.indicators.ma.period}
                            onChange={(e) => handleIndicatorChange('ma', 'period', parseInt(e.target.value))}
                            min="1"
                            max="200"
                        />
                    </div>

                    <div className="settings-section">
                        <h3>RSI</h3>
                        <label>
                            <input
                                type="checkbox"
                                checked={settings.indicators.rsi.enabled}
                                onChange={(e) => handleIndicatorChange('rsi', 'enabled', e.target.checked)}
                            />
                            활성화
                        </label>
                        <input
                            type="number"
                            value={settings.indicators.rsi.period}
                            onChange={(e) => handleIndicatorChange('rsi', 'period', parseInt(e.target.value))}
                            min="1"
                            max="50"
                        />
                    </div>

                    <div className="settings-section">
                        <h3>MACD</h3>
                        <label>
                            <input
                                type="checkbox"
                                checked={settings.indicators.macd.enabled}
                                onChange={(e) => handleIndicatorChange('macd', 'enabled', e.target.checked)}
                            />
                            활성화
                        </label>
                        <div className="macd-settings">
                            <input
                                type="number"
                                value={settings.indicators.macd.fastPeriod}
                                onChange={(e) => handleIndicatorChange('macd', 'fastPeriod', parseInt(e.target.value))}
                                min="1"
                                max="50"
                            />
                            <input
                                type="number"
                                value={settings.indicators.macd.slowPeriod}
                                onChange={(e) => handleIndicatorChange('macd', 'slowPeriod', parseInt(e.target.value))}
                                min="1"
                                max="50"
                            />
                        </div>
                    </div>

                    <div className="settings-section">
                        <h3>볼린저 밴드</h3>
                        <label>
                            <input
                                type="checkbox"
                                checked={settings.indicators.bollinger.enabled}
                                onChange={(e) => handleIndicatorChange('bollinger', 'enabled', e.target.checked)}
                            />
                            활성화
                        </label>
                        <input
                            type="number"
                            value={settings.indicators.bollinger.period}
                            onChange={(e) => handleIndicatorChange('bollinger', 'period', parseInt(e.target.value))}
                            min="1"
                            max="50"
                        />
                        <input
                            type="number"
                            value={settings.indicators.bollinger.multiplier}
                            onChange={(e) => handleIndicatorChange('bollinger', 'multiplier', parseFloat(e.target.value))}
                            min="0.1"
                            max="5"
                            step="0.1"
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default ChartSettings; 