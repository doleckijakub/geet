import { BasicResponse } from './basic-response';

export interface ResponseWithUsername extends BasicResponse {
    username?: string;
}