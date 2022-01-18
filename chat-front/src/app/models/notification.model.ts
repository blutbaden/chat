export interface Notification {
    content?: string | null;
    type: NotificationType;
    metadata?: Map<string, string> | string;
    time?: Date;
}

export enum NotificationType {
    USER_STATE = 'USER_STATE',
    ONLINE_USERS = 'ONLINE_USERS',
    INCOMING_MESSAGE = 'INCOMING_MESSAGE',
    INCOMING_CALL = 'INCOMING_CALL',
    REJECTED_CALL = 'REJECTED_CALL',
    CANCELLED_CALL = 'CANCELLED_CALL',
    ACCEPTED_CALL = 'ACCEPTED_CALL'
}
