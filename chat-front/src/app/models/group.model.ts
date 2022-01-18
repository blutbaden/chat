import {IUser} from "./user.model";

export interface IGroup {
  id?: number;
  name?: string | null;
  isActivated?: boolean | null;
  users?: IUser[] | null;
}

export class Group implements IGroup {
  constructor(public id?: number, public name?: string | null, public isActivated?: boolean | null, public users?: IUser[] | null) {
    this.isActivated = this.isActivated ?? false;
  }
}

export function getGroupIdentifier(group: IGroup): number | undefined {
  return group.id;
}
