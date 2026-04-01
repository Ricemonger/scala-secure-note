type LogContext = Record<string, unknown>;

interface Logger {
    info: (message: string, context?: LogContext) => void;
    warn: (message: string, context?: LogContext) => void;
    error: (error: Error | string, context?: LogContext) => void;
}

export const throwLoggedError = (error: Error): void => {
    logger.error(error);
    throw error;
};

export const logger: Logger = {

    info: (message, context) => {
        console.info(`%c[INFO] ${message}`, 'color: #3b82f6', context || '');
    },

    warn: (message, context) => {
        console.warn(`%c[WARN] ${message}`, 'color: #eab308', context || '');
    },

    error: (error, context) => {
        console.error(`%c[ERROR]`, 'color: #ef4444', error, context || '');
    },
};