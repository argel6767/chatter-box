'use client'
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/chat/input"
import { Label } from "@/components/ui/label"
import { ArrowLeft, Eye, EyeOff, MessageCircle } from "lucide-react"
import { useRouter } from "next/navigation"
import { useState } from "react"
import {AuthenticateUserDto} from "@/lib/models/requests";
import {login, register} from "@/api/auths";
import {sleep} from "@/lib/utils";
import {useLoadingStore, useUserStore} from "@/hooks/stores";
import { LoadingSpinner} from "@/components/ui/loading";
import { AnnouncementMessage } from "@/components/ui/annoucementMessage"
import { useToggle } from "@/hooks/use-toggle"

export const SignUp = () => {
    const [formData, setFormData] = useState({
        email: "",
        username: "",
        password: "",
    });

    const [failedSignUp, setFailedSignUp] = useState({isFailed: false, message: ""});
    const [isShowPassword, setIsShowPassword] = useState(false);
    const [isRegistered, setIsRegistered] = useState(false);
    const {value:loading, toggleValue} = useToggle(false);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        toggleValue();
        const response = await register(formData);
        toggleValue();
        if ('errorMessage' in response.data) {
            const errorMessage = response.data.errorMessage;
            setFailedSignUp({isFailed: true, message: errorMessage});
            await sleep(2500);
            setFormData({email: "", username:"", password: ""});
            setFailedSignUp({isFailed: false, message: ""});
        }
        else {
            setIsRegistered(true);
        }
    }

      const isFieldsFilled = () => {
        return !(formData.email.length > 0 && formData.username.length > 0 && formData.password.length > 0)
      }

      if (failedSignUp.isFailed) {
          return <AnnouncementMessage message={failedSignUp.message} title={"Oops! Something went wrong"}/>
      }

      if (isRegistered) {
          return (
              <AnnouncementMessage title={"Account created successfully"}
                message={"Please look for an email from us to verify your account. If you don't receive an email. Please check your spam folder."}
              />
          )
      }

      if (loading) {
          return (
              <AnnouncementMessage title={"Creating your account..."}>
                  <LoadingSpinner  size={"lg"}/>
              </AnnouncementMessage>
          )
      }

    return (
        <main className="motion-preset-pop motion-duration-700">
            <Card className="bg-black/20 backdrop-blur-sm border-white/10">
                <CardHeader>
                    <CardTitle className="text-white text-center">Sign Up</CardTitle>
                    <CardDescription className="text-gray-400 text-center">Enter your information to create an
                        account</CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit}>
                    <CardContent className="space-y-4">
                    {/* Email field */}
              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-300">Email</Label>
                <Input id="email" name="email" type="email" value={formData.email} onChange={handleInputChange} required
                       className="bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400" placeholder="Enter your email"/>
              </div>
                {/* Username field */}
              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-300">Username</Label>
                <Input
                  id="username" name="username" type="text" value={formData.username} onChange={handleInputChange} required
                  className="bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400" placeholder="Enter your username"/>
              </div>
              {/* Password field */}
              <div className="space-y-2">
                <Label htmlFor="password" className="text-gray-300">Password</Label>
                <div className="relative">
                  <Input id="password" name="password" type={isShowPassword ? "text" : "password"} value={formData.password} onChange={handleInputChange} required
                    className="bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400 pr-10"
                    placeholder="Enter your password"/>
                  <Button type="button" variant="ghost" size="sm" className="absolute right-0 top-0 h-full px-3 text-gray-400 hover:text-white hover:bg-transparent"
                    onClick={() => setIsShowPassword((prev) => !prev)}
                  >{isShowPassword ? <Eye className="h-4 w-4" /> :  <EyeOff className="h-4 w-4" />}
                  </Button>
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex flex-col space-y-4 pt-4">
              <Button type="submit" className="w-full gradient-primary text-white hover:opacity-90 transition-opacity"
                disabled={isFieldsFilled()}>
                Create Account
              </Button>
            </CardFooter>
          </form>
    </Card>
   </main>
      )
}

export const Login = () => {

    const router = useRouter();
    const [formData, setFormData] = useState<AuthenticateUserDto>({
        username:"",
        password: "",
      });
    const {setUser} = useUserStore();

    const [failedLogin, setFailedLogin] = useState({isFailed: false, message: ""});
    const {value: isLoading, toggleValue} = useToggle(false);


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        toggleValue();
        const response = await login(formData);
        if ('errorMessage' in response.data) {
            toggleValue();
            const errorMessage = response.data.errorMessage;
            setFailedLogin({isFailed: true, message: errorMessage});
            await sleep(2500);
            setFormData({username:"", password: ""});
            setFailedLogin({isFailed: false, message: ""});
        }
        else {
            const userData = response.data;
            localStorage.setItem("user", JSON.stringify(userData));
            setUser(userData);
            router.push("/chats");
        }
    }


    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
          ...formData,
          [e.target.name]: e.target.value
        });
      };

      const isFieldsFilled = () => {
        return !(formData.username.length > 0 && formData.password.length > 0);
      }

      if (failedLogin.isFailed) {
          return  (
              <AnnouncementMessage title={"Something went wrong!"} message={failedLogin.message}/>
          )
      }

      if (isLoading) {
          return (
              <AnnouncementMessage title={"Logging you in..."}>
                <LoadingSpinner  size={"lg"}/>
              </AnnouncementMessage>)
      }
    
    return (
        <main className="motion-preset-pop motion-duration-700">
            <Card className="bg-black/20 backdrop-blur-sm border-white/10">
          <CardHeader>
            <CardTitle className="text-white text-center">
              Sign In
            </CardTitle>
            <CardDescription className="text-gray-400 text-center">
               Enter your information to enter ChatterBox
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">

              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-300">Username</Label>
                <Input
                  id="username"
                  name="username"
                  type="text"
                  value={formData.username}
                  onChange={handleInputChange}
                  required
                  className="bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400"
                  placeholder="Enter your username"
                />
              </div>

              {/* Password field */}
              <div className="space-y-2">
                <Label htmlFor="password" className="text-gray-300">Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={"password"}
                    value={formData.password}
                    onChange={handleInputChange}
                    required
                    className="bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400 pr-10"
                    placeholder="Enter your password"
                  />
                </div>
              </div>
            </CardContent>

            <CardFooter className="flex flex-col space-y-4 pt-4">
              <Button
                type="submit"
                className="w-full gradient-primary text-white hover:opacity-90 transition-opacity"
                disabled={isFieldsFilled()}
              >
                Login
              </Button>
            </CardFooter>
            </form>
            </Card>
        </main>
    )
}

export const AuthToggle = () => {
    const [isSignUp, setIsSignUp] = useState(false);
    const {loading} = useLoadingStore()

    return (
        <main>
            <header className="flex flex-col justify-center items-center gap-2 pb-4">
                <Logo/>
                <p className="text-gray-400 text-center">
                    {isSignUp ? "Create your account to get started" : "Welcome back! Please sign in to your account"}
                </p>
            </header>
            {isSignUp ? <SignUp /> :
            <Login/>}
            <div className="text-center py-2">
                <span className="text-gray-400">
                  {isSignUp? "Already have an account" : "Don't have an account"}
                </span>
                <Button
                  type="button"
                  variant="link"
                  onClick={() => setIsSignUp((prev) => !prev)}
                  className="text-slate-400 hover:text-white p-0 ml-2"
                  disabled={loading}
                >
                  {isSignUp ? "Sign In" : "Sign Up"}
                </Button>
              </div>
        </main>
    )
}

export const Logo = () => {
    return (
        <div className="flex items-center justify-center space-x-2 mb-4">
            <MessageCircle className="h-8 w-8 text-slate-400" />
            <span className="text-2xl font-bold text-white">ChatterBox</span>
          </div>
    )
}

export const BackButton = () => {
    const router = useRouter();
    return (
        <div className="text-center mb-8">
        <Button
          variant="ghost"
          onClick={() => router.push("/")}
          className="absolute top-4 left-4 text-gray-400 hover:text-white hover:bg-white/10"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
      </div>
    )
}