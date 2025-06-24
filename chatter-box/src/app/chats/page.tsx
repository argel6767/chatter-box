import { ChatRoomList, DirectMessageList, GoBackHome, Header } from "./components";

const ChatList = () => {  

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <div className="flex h-screen">
        {/* Header */}
        <div className="w-80 bg-black/20 backdrop-blur-sm border-r border-white/10 flex flex-col motion-translate-x-in-[-43%] motion-translate-y-in-[0%] overflow-auto">
          {/* Header */}
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
        {/* Main Content */}
        <GoBackHome/>
      </div>
    </div>
  );
};

export default ChatList;