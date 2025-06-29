import {create} from 'zustand'
import {User} from '@/lib/models/models'
import { FriendshipDto } from '@/lib/models/responses';

interface SearchQueryStore {
    searchQuery: string;
    setSearchQuery: (query: string) => void;
}

export const useSearchQueryStore = create<SearchQueryStore>((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }));

interface UserStore {
    user: User;
    setUser: (newUser: User) => void;
    removeUser: () => void;
}

export const useUserStore = create<UserStore>((set) => ({
    user: typeof window !== 'undefined' && localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : {},
    setUser: (newUser: User) => set(() => ({ user: newUser })),
    removeUser: () => set(() => ({ user: undefined }))
}));

interface LoadingStore {
    loading: boolean;
    setLoading: (loading: boolean) => void;
    toggleLoading: () => void;
}

export const useLoadingStore = create<LoadingStore>((set) => ({
    loading: false,
    setLoading: (loading: boolean) => set(() => ({ loading })),
    toggleLoading: () => set((state) => ({ loading: !state.loading }))
}));

interface FriendStore {
    friends: FriendshipDto[];
    setFriends: (friends: FriendshipDto[]) => void;
    removeFriend: (friendship: FriendshipDto) => void;
    addFriend: (friendship: FriendshipDto) => void;
}

export const useFriendStore = create<FriendStore>((set) => ({
    friends:[],
    setFriends: (friends: FriendshipDto[]) => set(() => ({friends})),
    removeFriend: (friendship: FriendshipDto) => set((state) => ({friends: state.friends.filter(friend => friend.id !== friendship.id)})),
    addFriend: (friendship: FriendshipDto) => set((state) => ({friends: [...state.friends, friendship]}))
}));


