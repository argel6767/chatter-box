import { ArrowRight } from "lucide-react"
import Link from "next/link"

export const Hero = () => {
    return (
        <main className="motion-preset-fade motion-duration-1500">
            <div className="absolute inset-0 bg-gradient-to-r from-slate-600/20 to-slate-500/20 blur-3xl"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="animate-fade-in">
            <h1 className="text-4xl sm:text-6xl lg:text-7xl font-bold bg-gradient-to-r from-white via-slate-200 to-slate-300 bg-clip-text text-transparent mb-6">
              ChatterBox
            </h1>
            <p className="text-xl sm:text-2xl text-gray-300 mb-8 max-w-3xl mx-auto">
              Experience the future of communication with ChatterBox - where conversations flow seamlessly and connections matter.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
              <Link
                className="gradient-primary text-white px-8 py-4 text-lg border-1 border-accent rounded-2xl hover:opacity-90 transition-opacity group flex items-center justify-center"
                href={'/auth'}
              >
                Start Chatting
                <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
              </Link>
            </div>
          </div>
        </div>
        </main>
    )
}