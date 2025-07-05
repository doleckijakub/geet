import { Component, inject } from '@angular/core';

import { AuthStore } from '../auth/auth-store';

@Component({
  selector: 'app-home',
  template: `
    <div class="home">
      @if(authStore.username() === null) {
        <span>Not Logged in</span>
      } @else {
        <span>Logged in as &#64;{{ authStore.username() }}</span>
      }
    </div>
  `,
})
export default class Home {
  public authStore = inject(AuthStore);

  constructor() {}
}
