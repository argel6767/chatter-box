'use client'
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {Dialog,DialogContent,DialogDescription,DialogHeader,DialogTitle,DialogTrigger,} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/chat/input"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import {useSearchQueryStore, useUserStore} from "@/hooks/stores"
import { Badge, MessageCircle, Plus, Search, Settings } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useState } from "react"
import { NewChatDto } from "@/lib/models/requests"


const DropDownSettings = () => {
  return (
      <DropdownMenu>
      <DropdownMenuTrigger>
        <Settings className="h-6 w-6 text-gray-400 hover:text-slate-300 hover:bg-white/10 rounded-full " />
      </DropdownMenuTrigger>
        <DropdownMenuContent className="bg-gradient-to-br from-slate-800 via-slate-700 to-slate-800 text-slate-200">
          <DropdownMenuLabel>My Account</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem className="hover:bg-red-200">Profile</DropdownMenuItem>
          <DropdownMenuItem>Settings</DropdownMenuItem>
          <DropdownMenuItem>Logout</DropdownMenuItem>
        </DropdownMenuContent>
    </DropdownMenu>
    )
}

export const Header = () => {

    const searchQuery = useSearchQueryStore((state) => state.searchQuery);
    const setSearchQuery = useSearchQueryStore((state) => state.setSearchQuery)

    return (
          <main>
            <div className="p-5 border-b border-white/10">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2 py-2">
                <MessageCircle className="h-7 w-7 text-slate-400" />
                <Link className="text-2xl font-semibold text-slate-200 hover:underline hover:underline-offset-8" href={"/chats"}>ChatterBox</Link>
              </div>
              <DropDownSettings/>
            </div>
            
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search conversations..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400"
              />
            </div>
          </div>
          </main> 
    )
}



export const GoBackHome = () => {
    const router = useRouter();
    return (
        <div className="flex-1 flex items-center justify-center bg-black/10">
          <div className="text-center">
            <MessageCircle className="h-24 w-24 text-slate-400/50 mx-auto mb-6" />
            <h2 className="text-2xl font-semibold text-white mb-2">Select a conversation</h2>
            <p className="text-gray-400 mb-8 max-w-md">
              Choose from your chat rooms or direct messages to start connecting with your team.
            </p>
            <Button 
              className="gradient-primary text-white hover:opacity-90 transition-opacity hover:cursor-pointer"
              onClick={() => router.push('/')}
            >
              Back to Home
            </Button>
          </div>
        </div>
    )
}

const NewChatSheet = () => {
  const {user} = useUserStore();
  const [formData, setFormData] = useState<NewChatDto>(
    {name:"",
    usernames: [user.username]}
  )
  return (
      <Dialog>
        <DialogTrigger>
          <Plus className="h-6 w-6 text-gray-400 rounded-full hover:slate-300 hover:cursor-pointer" />
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="text-center">Create New Chat</DialogTitle>
            <DialogDescription>
              This action cannot be undone. This will permanently delete your account
              and remove your data from our servers.
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      </Dialog>
  )
}

export const ChatRoomList = () => {
    const {user} = useUserStore();
    const chatRooms = user.chatRooms;
    const router = useRouter();
    const searchQuery:string = useSearchQueryStore((state) => state.searchQuery);
    
    const filteredRooms = chatRooms.filter(room => 
        room.name.toLowerCase().includes(searchQuery)
      );

    const handleChatClick = (chatId: number) => {
        router.push(`/chats/room/${chatId}`);
      };

    return (
        <main>
            <div className="p-4 motion-preset-blur-right motion-duration-500">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-medium text-gray-300 uppercase tracking-wide">Chat Rooms</h3>
                <NewChatSheet/>
              </div>
              
              <div className="space-y-1">
                {filteredRooms.map((room) => (
                  <Card
                    key={room.id}
                    className="bg-transparent border-none cursor-pointer hover:bg-white/5 transition-colors animate-slide-in"
                    onClick={() => handleChatClick(room.id)}
                  >
                    <CardContent className="p-3">
                      <div className="flex items-center space-x-3">
                        <div className="relative">
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-lg font-medium text-slate-200 truncate">{room.name}</p>
                          </div>
                          <div className="flex items-center justify-between mt-1">
                            <div className="flex items-center space-x-1 text-xs text-gray-500">
                            </div>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
        </main>
    )
}

export const DirectMessageList = () => {
    const directMessages = []

    const router = useRouter();
   // const [directMessages, setDirectMessages] = useState<any>([]);
    const searchQuery:string = useSearchQueryStore((state) => state.searchQuery);

    const handleChatClick = (chatId: number, type: 'room' | 'dm') => {
        router.push(`/chats/${type}/${chatId}`);
      };

      const filteredDMs = directMessages.filter(dm => 
        dm.name.toLowerCase().includes(searchQuery)
      );

    return (
        <main>
            <div className="flex-1 overflow-y-auto motion-preset-blur-right motion-duration-500">
            <div className="p-4 border-t border-white/10">
              <div className="flex items-center justify-between mb-3">
                <h3 className="text-sm font-medium text-gray-300 uppercase tracking-wide">Direct Messages</h3>
                <Button size="sm" variant="ghost" className="text-gray-400 hover:text-white hover:bg-white/10 p-1">
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
              
              <div className="space-y-1">
                {filteredDMs.map((dm) => (
                  <Card
                    key={dm.id}
                    className="bg-transparent border-none cursor-pointer hover:bg-white/5 transition-colors animate-slide-in"
                    onClick={() => handleChatClick(dm.id, 'dm')}
                  >
                    <CardContent className="p-3">
                      <div className="flex items-center space-x-3">
                        <div className="relative">
                          <Avatar className="h-8 w-8">
                            <AvatarImage src={dm.avatar} alt={dm.name} />
                            <AvatarFallback className="bg-slate-500 text-white text-xs">
                              {dm.name.split(' ').map(n => n[0]).join('')}
                            </AvatarFallback>
                          </Avatar>
                          {dm.isOnline && (
                            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 border-2 border-gray-900 rounded-full"></div>
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-white truncate">{dm.name}</p>
                            {dm.unreadCount > 0 && (
                              <Badge className="bg-slate-500 text-white text-xs px-1.5 py-0.5 rounded-full">
                                {dm.unreadCount}
                              </Badge>
                            )}
                          </div>
                          <p className="text-xs text-gray-400 truncate">{dm.lastMessage}</p>
                          <span className="text-xs text-gray-500">{dm.timestamp}</span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
              </div>
            </div>
        </main>
    )
}