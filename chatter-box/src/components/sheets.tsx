'use client'

import {useFriendStore, useUserStore} from "@/hooks/stores";
import {useState} from "react";
import {NewChatDto} from "@/lib/models/requests";
import {useFailedRequest} from "@/hooks/use-failed-request";
import {useToggle} from "@/hooks/use-toggle";
import {useRouter} from "next/navigation";
import {createChatRoom} from "@/api/chatroom";
import {isFailedResponse, sleep} from "@/lib/utils";
import {ChatRoom} from "@/lib/models/models";
import {ChatRoomDto} from "@/lib/models/responses";
import {Dialog, DialogClose, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Loading} from "@/components/ui/loading";
import {Plus, Search, X} from "lucide-react";
import {Label} from "@/components/ui/label";
import {Input} from "@/components/ui/chat/input";
import {SearchForUser} from "@/components/search-for-user";
import {Button} from "@/components/ui/button";
import {MemberList, UsersList} from "@/app/(protected)/chats/components";

export const NewChatSheet = () => {
    const {user, addChat} = useUserStore();
    const {friends} = useFriendStore();
    const [formData, setFormData] = useState<NewChatDto>(
        {name:"",
            usernames: [user.username ? user.username : ""]}
    )
    const {failedRequest, updateFailedRequest, resetFailedRequest} = useFailedRequest();
    const {value: isLoading, toggleValue:toggleLoading} = useToggle(false);
    const router = useRouter();

    const handleName = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData(prevFormData => ({
            ...prevFormData,
            name: e.target.value
        }))
    }

    const addMember = (username: string) => {
        setFormData(prevFormData => ({
            ...prevFormData,
            usernames: Array.from(new Set([...prevFormData.usernames, username]))
        }))
    }

    const removeMember = (username: string) => {
        if (username === user.username) {
            return; //cant remove yourself if making a chat
        }
        setFormData(prevFormData => ({
            ...prevFormData,
            usernames: prevFormData.usernames.filter(name => name !== username)
        }))
    }

    const resetForm = () => {
        setFormData(prevFormData => ({
            ...prevFormData,
            name: "",
            usernames: [user.username ? user.username : ""]
        }))
    }

    const createNewChat = async(e: React.FormEvent) => {
        e.preventDefault();
        toggleLoading();
        const response = await createChatRoom(formData);
        if (isFailedResponse(response)) {
            toggleLoading();
            updateFailedRequest(true, "Sorry, your chat failed to be created! Try again later.");
            await sleep(2000);
            resetFailedRequest();
        }
        else {
            const data = response.data as ChatRoom;
            const chatDto: ChatRoomDto = {id: data.id, name:data.name};
            addChat(chatDto);
            router.push(`/chats/room/${data.id}`);
        }
    }

    if (isLoading) {
        return (
            <Dialog>
                <DialogContent className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-200" showCloseButton={false} >
                    <DialogHeader className={"flex flex-col justify-center items-center gap-4"}>
                        <DialogTitle className={"text-2xl"}>Hang Tight!</DialogTitle>
                        {formData.name === "" ? <p>Your new ChatRoom is being created!</p> :
                            <p>{"Your ChatRoom " + formData.name + " is being created!"}</p>}
                        <Loading variant={"dots"} size={"xl"} />
                    </DialogHeader>
                </DialogContent>
            </Dialog>
        )
    }

    if (failedRequest.isFailed) {
        return (
            <Dialog>
                <DialogContent className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-200" showCloseButton={false} >
                    <DialogHeader className={"flex flex-col justify-center items-center gap-4"}>
                        <DialogTitle className={"text-2xl"}>Something went wrong!</DialogTitle>
                        <p>{failedRequest.message}</p>
                    </DialogHeader>
                </DialogContent>
            </Dialog>
        )
    }

    return (
        <Dialog>
            <DialogTrigger>
                <Plus className="h-6 w-6 text-gray-400 rounded-full hover:slate-300 hover:cursor-pointer" />
            </DialogTrigger>
            <DialogContent className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-200" showCloseButton={false}>
                <DialogHeader>
                    <DialogClose className="ring-offset-background focus:ring-ring data-[state=open]:bg-accent data-[state=open]:text-muted-foreground absolute top-4 right-4 rounded-xs opacity-70 transition-opacity hover:opacity-100 focus:ring-2 focus:ring-offset-2 focus:outline-hidden disabled:pointer-events-none [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"
                                 onClick={resetForm}><X className="h-4 w-4" />
                    </DialogClose>
                    <DialogTitle className="text-center py-2 text-2xl">Create New Chat</DialogTitle>
                    <form onSubmit={createNewChat} className="flex flex-col justify-center gap-6">
              <span className="flex flex-col justify-center gap-2">
                <Label className="text-md" htmlFor="name">Name your chat (optional)</Label>
                <Input id="name" name="name" value={formData.name} onChange={handleName} placeholder="ex. Coolest Chat Ever"/>
              </span>
                        <section className="flex flex-col gap-2">
                            <MemberList usernames={formData.usernames} onClick={removeMember}/>
                            <SearchForUser labelText="Other Users" addMember={addMember}>
                  <span className="border-b-2 flex flex-col gap-2">
                    <Label className="text-md">Your friends</Label>
                    <UsersList friends={friends} addToList={addMember}/>
                  </span>
                            </SearchForUser>
                        </section>
                        <Button type="submit">Create Chat</Button>
                    </form>
                </DialogHeader>
            </DialogContent>
        </Dialog>
    )
}

export const FindUserSheet =  () => {

    const router = useRouter();
    const navigateToProfile = (id: number) => {
        router.push(`/profiles/${id}`);
    }

    return (
        <Dialog>
            <DialogTrigger>
                <nav className={"flex justify-start items-center gap-2 hover:cursor-pointer hover:underline hover:underline-offset-4"}>
                    <Search className={"h-4 w-4"}/>
                    <p>Find a user</p>
                </nav>
            </DialogTrigger>
            <DialogContent className="bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-200">
                <DialogTitle className={"text-center text-2xl"}>Looking for someone?</DialogTitle>
                <SearchForUser labelText={""} seeProfile={navigateToProfile}/>
            </DialogContent>
        </Dialog>
    )
}