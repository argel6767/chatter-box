import { useMemo, useState } from "react"
import debounce from "lodash.debounce";
import { useGetUserQuery } from "@/hooks/react-query";
import { Label } from "@radix-ui/react-label";
import { Search } from "lucide-react";
import { Input } from "./ui/chat/input";
import { AnnouncementMessage } from "./ui/annoucementMessage";
import { Loading } from "./ui/loading";
import { UsersList } from "@/app/(protected)/chats/components";
import { QueriedUserDto } from "@/lib/models/responses";
import { isFailedResponse } from "@/lib/utils";

interface SearchForUserProps {
    labelText: string;
    children: React.ReactNode
    onClick: (entity: string) => void;
}

export const SearchForUser = ({labelText, children, onClick}: SearchForUserProps) => {
    const [input, setInput] = useState<string>("");
    const [query, setQuery] = useState<string>("");
    const {isFetching, isError, data} = useGetUserQuery(query);

    // Debounce the search input
  const debouncedSetSearch = useMemo(
    () => debounce(setQuery, 500), // 500ms debounce
    []
  );

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInput(e.target.value);
    debouncedSetSearch(e.target.value);
  };

    const isQuerying = () => {
        if (isFetching) {
        return (
            <main className="flex justify-center items-center">
                <AnnouncementMessage title={"Searching for users..."}>
                    <Loading size="default" variant="dots"/>
                </AnnouncementMessage>
            </main>
        )
        }
    }


  const isFailed = () => {
    if (isError || (data && isFailedResponse(data))) {
        return (
            <main className="flex justify-center items-center">
                <AnnouncementMessage title={"Something went wrong!"} message="Could not search users. Try again later.">
                </AnnouncementMessage>
            </main>
        )
      }
  }

  const isFetched = () => {
    if (data && !isFailedResponse(data)) {
        return (
            <UsersList users={data.data as QueriedUserDto[]} onClick={onClick}/>
        )
    }
  }

  return (
    <main className="flex flex-col gap-4 motion-preset-fade motion-duration-500">
        <section className="flex flex-col gap-2 border-b-2 pb-2">
        <Label>Add members</Label>
            <span className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input placeholder="ex. User123"  className="pl-10 bg-white/5 border-white/10 text-white placeholder-gray-400 focus:border-slate-400" onChange={handleInputChange} value={input}/>
            </span>
        </section>
        {children}
        <section className="flex flex-col gap-2">
            <Label className="text-md">{labelText}</Label>
            {isFailed()}
            {isQuerying()}
            {isFetched()}
        </section>
       
    </main>
  )
}