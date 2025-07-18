'use client'
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/chat/input"
import { DropdownMenu, DropdownMenuContent, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import {useClearStores, useFriendStore, useSearchQueryStore, useUserStore} from "@/hooks/stores"
import {MessageCircle, Search, Settings, Users} from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { FailedRequest, useFailedRequest } from "@/hooks/use-failed-request"
import { useToggle } from "@/hooks/use-toggle"
import { useGetFriendRequests, useGetFriends } from "@/hooks/react-query"
import {isFailedResponse} from "@/lib/utils"
import {DirectMessageDto, FriendshipDto, QueriedUserDto} from "@/lib/models/responses"
import { Loading } from "@/components/ui/loading"
import { ChatBubbleAvatar } from "@/components/ui/chat/chat-bubble"
import { toast } from "sonner"
import { logout } from "@/api/auths"
import {FindUserSheet, NewChatSheet} from "@/components/sheets";

const Logout =  () => {

  const {value:isLoading, toggleValue: toggleLoading} = useToggle(false);
  const router = useRouter();
  const clearStores = useClearStores();

  const handleLogout = async() => {
    toggleLoading();
    const response = await logout();
    if (!isFailedResponse(response)) {
      clearStores();
      router.push("/");
    }
    else {
      toast("Failed to logout! Redirecting to landing page.");
      clearStores();
      router.replace("/")
    }
  }
  
  return (
    <button className="hover:bg-slate-700 hover:cursor-pointer text-left disabled:cursor-not-allowed flex items-center gap-2" onClick={handleLogout} disabled={isLoading}>Logout {isLoading && <Loading variant="default" size="sm"/>}</button>
  )

}


const DropDownSettings = () => {
  return (
      <DropdownMenu>
      <DropdownMenuTrigger>
        <Settings className="h-6 w-6 text-gray-400 hover:text-slate-300 hover:bg-white/10 rounded-full " />
      </DropdownMenuTrigger>
        <DropdownMenuContent className="bg-gradient-to-br from-slate-800 via-slate-700 to-slate-800 text-slate-200">
          <DropdownMenuLabel className="text-lg">My Account</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <nav className="flex flex-col gap-2 p-2">
            <Link className="hover:bg-slate-700" href={"/profile"}>Profile</Link>
            <Link className="hover:bg-slate-700" href={"/settings"}>Settings</Link>
            <Logout/>
          </nav>
        </DropdownMenuContent>
    </DropdownMenu>
    )
}

interface FriendContainerProps {
  isLoading: boolean
  friends: FriendshipDto[]
  failedRequest: FailedRequest
}

const FriendContainer = ({isLoading, friends, failedRequest}: FriendContainerProps) => {

  if (failedRequest.isFailed) {
    return (
      <main className="size-full p-4">
        <p className="text-red-400">{failedRequest.message}</p>
      </main>
    )
  }

  if (isLoading) {
    return (
      <div className="size-full py-2">
        <Loading/>
      </div>
    )
  }
  
  return (
    <ul>
      {friends.map((friendship) => {
          const friend = friendship.friend;
          const fallBack = friend.username.substring(0,1).toUpperCase();
        return (
          <li className="flex justify-around items-center gap-3 p-4 hover:bg-slate-700 hover:cursor-pointer rounded-lg" key={friend.id}>
              <Link className={"flex gap-3 justify-around items-center"} href={`/profiles/${friend.id}`}>
                  <ChatBubbleAvatar className="bg-slate-200 text-black" fallback={fallBack}/>
                <p>{friend.username}</p>
              </Link>
          </li>
        )
      })}
    </ul>
  )
}

const FriendsDropDown = () => {
  const {user} = useUserStore();
  const friendsQuery = useGetFriends(user.id);
  const requestsQuery = useGetFriendRequests(user.id);
  const {friends, setFriends} = useFriendStore();
  const [friendRequests, setFriendRequests] = useState<FriendshipDto[]>([]);
  const failedFriendsQuery = useFailedRequest();
  const failedRequestsQuery = useFailedRequest();

  useEffect(() => {
    const data = friendsQuery.data;
    if (data) {
      if (!isFailedResponse(data)) {
        const friendData: FriendshipDto[] = data.data as FriendshipDto[];
        setFriends(friendData);
      }
      else {
        failedFriendsQuery.updateFailedRequest(true, "Could not fetch friends!")
      }
    }
    
  }, [friendsQuery.data, setFriends])

  useEffect(() => {
    const data = requestsQuery.data;
    if (data) {
      if (!isFailedResponse(data)) {
        const friendData: FriendshipDto[] = data.data as FriendshipDto[];
        setFriendRequests(friendData);
      }
      else {
        failedRequestsQuery.updateFailedRequest(true, "Could not fetch friend requests!")
      }
    }
    
  }, [requestsQuery.data])

  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <Users className="h-6 w-6 text-gray-400 hover:text-slate-300 hover:bg-white/10 rounded-full " />
      </DropdownMenuTrigger>
      <DropdownMenuContent className="bg-gradient-to-br from-slate-800 via-slate-700 to-slate-800 text-slate-200 flex flex-col gap-2 ">
        <FindUserSheet/>
          <DropdownMenuSeparator />
        <section>
          <DropdownMenuLabel className="text-lg">Friend Requests</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <FriendContainer isLoading={requestsQuery.isFetching} friends={friendRequests} failedRequest={failedRequestsQuery.failedRequest}/>
        </section>
        <section>
          <DropdownMenuLabel className="text-lg">Friends</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <FriendContainer isLoading={friendsQuery.isFetching} friends={friends} failedRequest={failedFriendsQuery.failedRequest}/>
        </section>
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
                <Link className="text-2xl font-semibold text-slate-200 hover:underline hover:underline-offset-8"
                 href={"/chats"}>ChatterBox</Link>
              </div>
              <nav className="flex justify-end gap-2">
                <FriendsDropDown/>
                <DropDownSettings/>
              </nav>
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

interface MemberListProps {
    usernames: string[];
    onClick: (username: string) => void;
}

export const MemberList = ({usernames, onClick}: MemberListProps) => {
    const handleClick = (username: string) => {
        onClick(username);
        toggleHover();
    }
    const {value:isHovering, toggleValue:toggleHover} = useToggle(false);

    return (
        <ul className="flex gap-2">
          {usernames.map((username) => {
            const fallBack = username.substring(0,1).toUpperCase();
            return (
              <li className="flex justify-start items-center gap-2 p-2 hover:bg-slate-700 hover:cursor-pointer rounded-lg"
                  key={username} onClick={() => handleClick(username)} onMouseEnter={toggleHover} onMouseLeave={toggleHover}>
                <ChatBubbleAvatar className={`bg-slate-200 ${isHovering? "text-red-500" : "text-black"}`} fallback={isHovering? "X" : fallBack}/>
              </li>
            )
          })}
        </ul>
    )
}


interface UserListProps {
  friends?: FriendshipDto[];
  users?: QueriedUserDto[];
  addToList?: (entity: string) => void;
  goToProfile?: (entity: number) => void;
}

export const UsersList = ({friends=[], users=[], addToList, goToProfile}: UserListProps) => {

    const handleOnClick = (username: string, id: number) => {
        if (addToList) {
            addToList(username);
        }
        else if (goToProfile) {
            goToProfile(id);
        }
        else {
            throw Error("Both functions cannot be undefined")
        }
    }

    return (
        <main>
          <ul>
          {friends.map((friendship) => {
              const friend = friendship.friend;
              const fallBack = friend.username.substring(0,1).toUpperCase();
            return (
              <li className="flex justify-around items-center gap-3 p-4 hover:bg-slate-700 hover:cursor-pointer rounded-lg text-sm" key={friend.id} onClick={() => handleOnClick(friend.username, friend.id)}>
                <ChatBubbleAvatar className="bg-slate-200 text-black" fallback={fallBack}/>
                <p>{friend.username}</p>
              </li>)})}
          </ul>
          <ul>
          {users.map((user) => {
              const fallBack = user.username.substring(0,1).toUpperCase();
            return (
              <li className="flex justify-around items-center gap-3 p-4 hover:bg-slate-700 hover:cursor-pointer rounded-lg text-sm" key={user.id} onClick={() => handleOnClick(user.username, user.id)}>
                <ChatBubbleAvatar className="bg-slate-200 text-black" fallback={fallBack}/>
                <p>{user.username}</p>
              </li>)})}
          </ul>
        </main>
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
    const directMessages: DirectMessageDto[] = []

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
                <h3 className="text-sm font-medium text-gray-300 uppercase tracking-wide">Direct Messages (coming soon)</h3>
                  {/*<Button size="sm" variant="ghost" className="text-gray-400 hover:text-white hover:bg-white/10 p-1">
                      <Plus className="h-4 w-4"/>
                  </Button>*/}
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
                            <AvatarFallback className="bg-slate-500 text-white text-xs">
                              {dm.name.split(' ').map(n => n[0]).join('')}
                            </AvatarFallback>
                          </Avatar>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <p className="text-sm font-medium text-white truncate">{dm.name}</p>
                            
                          </div>
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
