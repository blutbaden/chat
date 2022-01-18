import {IUser} from "./user.model";
import {IRoom} from "./room.model";
import {ConversationState} from "./conversation-state.model";

export interface IConversation {
  id?: number;
  content?: string | null;
  sender?: IUser | null;
  receiver?: IUser | null;
  conversationState?: ConversationState;
  room?: IRoom | null;
  createdDate?: Date;
}

export class Conversation implements IConversation {
  constructor(public id?: number, public content?: string | null, public sender?: IUser | null, public receiver?: IUser | null, conversationState?: ConversationState, public room?: IRoom | null, public createdDate?: Date) {
  }
}

export function getConversationIdentifier(conversation: IConversation): number | undefined {
  return conversation.id;
}
