import { AuthToggle, BackButton} from "./components";

const Auth = () => {
  
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <BackButton path={"/"}/>
        <AuthToggle/>
        </div>
    </div>
  );
};

export default Auth;
