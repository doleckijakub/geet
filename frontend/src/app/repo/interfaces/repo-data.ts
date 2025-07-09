import { RepoEntry } from './repo-entry';

export interface RepoData {
  owner: string;
  name: string;
  visibility: 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
  defaultBranch: string;
  entries: RepoEntry[];
}