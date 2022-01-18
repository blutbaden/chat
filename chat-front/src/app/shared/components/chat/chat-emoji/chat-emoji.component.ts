import {Component, EventEmitter, Input, OnChanges, OnInit, Output} from '@angular/core';
import {EmojiUtil} from "../../../../core/util/emoji";

@Component({
  selector: 'app-chat-emoji',
  templateUrl: './chat-emoji.component.html',
  styleUrls: ['./chat-emoji.component.scss']
})
export class ChatEmojiComponent implements OnInit, OnChanges {

  @Input() popupAnchor = 'top';
  @Input() onEnter: Function = () => {};
  @Input() model: any;
  @Output() modelChange: any = new EventEmitter();

  input?: string;
  filterEmojis?: string;
  emojiUtil: EmojiUtil = new EmojiUtil();
  allEmojis?: Array<any>;
  popupOpen: boolean = false;

  ngOnInit() {
    this.input = '';
    this.filterEmojis = '';
    this.allEmojis = this.emojiUtil.getAll();
  }

  ngOnChanges() {
    if (this.model !== this.input) {
      this.input = this.model;
    }
  }

  togglePopup() {
    this.popupOpen = !this.popupOpen;
  }

  getFilteredEmojis() {
    return this.allEmojis?.filter((e) => {
      if (this.filterEmojis === '') {
        return true;
      } else {
        for (let alias of e.aliases) {
          if (alias.includes(this.filterEmojis)) {
            return true;
          }
        }
      }
      return false;
    });
  }

  onEmojiClick(e: any) {
    this.input = this.input + e;
    this.modelChange.emit(this.input);
    this.popupOpen = false;
  }

  onChange(newValue: any) {
    this.input = this.emojiUtil.emojify(newValue);
    this.model = this.input;
    this.modelChange.emit(this.input);
  }

}
