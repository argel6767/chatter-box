"use client"
import { Toaster as Sonner, ToasterProps } from "sonner"

const Toaster = ({ ...props }: ToasterProps) => {

  return (
    <Sonner
      className="bg-gradient-to-br from-slate-800 via-slate-700 to-slate-800 text-red-700"
      toastOptions={{unstyled:true, className:"bg-gradient-to-br from-slate-700 via-slate-600 to-slate-700 text-slate-100 p-2 shadow-lg rounded-lg"}}
      {...props}
    />
  )
}

export { Toaster }
