'use client'
import { MessageCircle } from "lucide-react"
import Link from "next/link"
import {ChatRoomList, DirectMessageList, Header} from "@/app/chats/components";


export const Navbar = () => {
    return (
        <nav className="border-b border-white/10 backdrop-blur-sm bg-black/20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-2">
              <MessageCircle className="h-8 w-8 text-slate-400" />
              <span className="text-2xl font-bold text-white">ChatterBox</span>
            </div>
            <div className="flex items-center space-x-4">
              <Link 
                className="text-white hover:text-slate-300 border-1 border-accent rounded-lg p-2"
                href={'/chats'}
              >
                Open App
              </Link>
              <Link 
                className="text-white hover:text-slate-300  border-1 border-accent rounded-lg p-2"
                href={'/auth'}
              >
                Get Started
              </Link>
            </div>
          </div>
        </div>
      </nav>
    )
}

