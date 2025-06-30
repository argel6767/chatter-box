import { Toaster } from "@/components/ui/sonner"

export default function ChatDashboardLayout({ children }: {children: React.ReactNode;}) {
  return (
      <main>
        <section>{children}</section>
          <Toaster position="top-center"/>
      </main>
  )
}