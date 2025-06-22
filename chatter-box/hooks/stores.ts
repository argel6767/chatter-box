import {create} from 'zustand'
import {User} from '@/lib/models/models'

export const useSearchQueryStore = create((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }));

interface UserStore {
    user: User;
    setUser: (newUser: User) => void;
}

export const useUserStore = create<UserStore>((set) => ({
    user: {
        email: '',
        username: '',
        chatRooms: [],
        authDetails: {
            id: 0,
            isVerified: false,
            roles: []
        }
    },
    setUser: (newUser: User) => set(() => ({ user: newUser }))
}));

