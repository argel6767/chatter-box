import Link from "next/link";


const NotFound = () => {

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4 text-slate-400">404</h1>
        <p className="text-xl text-gray-400 mb-4">Oops! Page not found</p>
        <Link href="/public" className="text-blue-500 hover:text-blue-700 underline">
          Return to Home
        </Link>
      </div>
    </div>
  );
};

export default NotFound;