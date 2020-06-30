import { Moment } from 'moment';
import { IAuthor } from 'app/shared/model/author.model';

export interface IBook {
  id?: number;
  title?: string;
  description?: string;
  publicationDate?: string;
  price?: number;
  author?: IAuthor;
}

export const defaultValue: Readonly<IBook> = {};
