import { ChatRoomList, DirectMessageList, Header } from "../../components";
import {ChatContainer} from "@/app/(protected)/chats/[type]/[chatId]/components";

type PageProps = {
  params: Promise<{ type: string; chatId: string }>
};

export default async function ChatPage({params}: PageProps) {
    const { chatId } = await params;

    // If you need to convert chatId to a number:
    const numericChatId = Number(chatId);
    
    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
            <section className="flex h-screen">
                <div className="w-96 bg-black/20 backdrop-blur-sm border-r border-white/10 flex flex-col motion-translate-x-in-[-43%] motion-translate-y-in-[0%] overflow-auto">
                      <section>
                        <Header/>
                      </section>
                      <section>
                        <ChatRoomList/>
                      </section>
                      <section>
                        <DirectMessageList/>
                      </section>
                </div>
                <section className={"h-full w-full px-2"}>
                    <ChatContainer id={numericChatId}/>
                </section>
            </section>
        </div>);
  }