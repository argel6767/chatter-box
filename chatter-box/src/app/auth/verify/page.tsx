import { BackButton} from "@/app/auth/components";
import {verify} from "@/api/auths";
import {VerifyUserDto} from "@/lib/models/requests";
import {AnnouncementMessage} from "@/components/ui/annoucementMessage";

interface VerifyResponse {
    status: number;
    message: string;
    success: boolean;
}

const verifyUser = async (email: string, username: string, code: string ) => {
    if (!code || !email || !username) {
        return {status: 400, message: "No token provided", success: false} as VerifyResponse;
    }
    const requestBody: VerifyUserDto = {email: email, username: username, code: code}
    const response = await verify(requestBody);
    if (typeof response.data === 'object' && 'errorMessage' in response.data && 'instance' in response.data) {
        return {status: response.statusCode, message: response.data.errorMessage, success: false} as VerifyResponse;
    }
    return {status: response.statusCode, message: response.data, success: true} as VerifyResponse;
}

interface VerifyDtoProps {
    email: string;
    username: string;
    code: string;
}

interface VerifyProps {
    searchParams: VerifyDtoProps;
}

const Verify = async ({searchParams}:VerifyProps) => {
    const email = searchParams.email;
    const username = searchParams.username;
    const code = searchParams.code;

    const response: VerifyResponse = await verifyUser(email, username, code);

    const getTitle = () => {
        if (response.success) {
            return "Account Verified!";
        }
        else {
            return "Verification Failed! Something went wrong.";
        }
    }

    return (<div
        className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
            <BackButton/>
            <AnnouncementMessage title={getTitle()} message={response.message}/>
        </div>
    </div>)
}
export default Verify;