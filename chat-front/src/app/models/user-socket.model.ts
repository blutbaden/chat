import {UserState} from "./user-state.model";

export interface IUserSocket {
  username?: string;
  state?: UserState;
}

export class UserSocket implements IUserSocket {
  constructor(public username?: string, public state?: UserState) {
  }
}
