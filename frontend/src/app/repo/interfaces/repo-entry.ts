export interface RepoEntry {
    name: string;
    lastCommitShaStr: string;
    updatedAt: string;
    type: 'FILE' | 'DIRECTORY' | 'SUBMODULE' | 'LINK';
}