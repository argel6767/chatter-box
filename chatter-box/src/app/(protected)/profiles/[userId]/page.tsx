import { UserInfo} from "@/app/(protected)/profiles/[userId]/components";
import {BackButton} from "@/app/auth/components";

type PageProps = {
    params: Promise<{ userId: string }>
};

export default async function UserProfile({params}: PageProps) {

    const {userId} = await params;
    const numericUserId = Number(userId);

    return (
        <main className={"min-h-screen flex flex-col items-center justify-start bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-slate-300 p-4 gap-6"}>
            <BackButton path={"/chats"}/>
            <UserInfo id={numericUserId}/>
        </main>)
}