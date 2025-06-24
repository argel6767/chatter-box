import { MessageCircle } from "lucide-react"

export const Footer = () => {
    return (
        <main>
        <footer className="bg-gradient-to-r from-slate-800/50 to-slate-700/50 backdrop-blur-sm rounded-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center space-x-2 mb-4 md:mb-0">
              <MessageCircle className="h-6 w-6 text-slate-400" />
              <span className="text-lg font-semibold text-white">ChatterBox</span>
            </div>
            <div className="text-gray-400 text-center md:text-right p-2">
              <p>&copy; 2024 ChatterBox. All rights reserved.</p>
              <p className="text-sm mt-1">Built with ❤️ for seamless communication</p>
            </div>
          </div>
        </div>
      </footer>
        </main>
    )
}