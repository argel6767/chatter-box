import { create } from 'zustand'

export const useSearchQuery = create((set) => ({
    searchQuery: "",
    setSearchQuery: (query: string) => set(() => ({ searchQuery: query.toLowerCase() }))
  }))

