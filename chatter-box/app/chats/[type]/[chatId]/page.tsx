import { ChatRoomList, DirectMessageList, Sidebar } from "../../components";
import {ChatContainer} from "@/app/chats/[type]/[chatId]/components";

export default function ChatPage({ 
    params 
  }: { 
    params: { type: string; id: string } 
  }) {
    const { type, id } = params;
    
    return (<div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
            <div className="flex h-screen">
            <div className="w-80 bg-black/20 backdrop-blur-sm border-r border-white/10 flex flex-col motion-translate-x-in-[-43%] motion-translate-y-in-[0%] overflow-auto">
          {/* Header */}
          <section>
            <Sidebar/>
          </section>
          <section>
            <ChatRoomList/>
          </section>
          <section>
            <DirectMessageList/>
          </section>
        </div>
            <ChatContainer/>
        </div>
        </div>);
  }