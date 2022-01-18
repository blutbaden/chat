import {IUser} from "./user.model";
import {IGroup} from "./group.model";

export interface IRoom {
  id?: number;
  name?: string | null;
  key?: string;
  isActivated?: boolean | null;
  allowImageMessage?: boolean | null;
  allowVoiceMessage?: boolean | null;
  allowStickerMessage?: boolean | null;
  users?: IUser[] | null;
  group?: IGroup | null;
  createdBy?: string | null;
}

export class Room implements IRoom {
  constructor(
    public id?: number,
    public name?: string | null,
    public key?: string,
    public isActivated?: boolean | null,
    public allowImageMessage?: boolean | null,
    public allowVoiceMessage?: boolean | null,
    public allowStickerMessage?: boolean | null,
    public users?: IUser[] | null,
    public group?: IGroup | null,
    public createdBy?: string | null
  ) {
    this.isActivated = this.isActivated ?? false;
    this.allowImageMessage = this.allowImageMessage ?? false;
    this.allowVoiceMessage = this.allowVoiceMessage ?? false;
    this.allowStickerMessage = this.allowStickerMessage ?? false;
  }
}

export function getRoomIdentifier(room: IRoom): number | undefined {
  return room.id;
}
