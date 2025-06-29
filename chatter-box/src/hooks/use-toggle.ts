import { useState } from "react";

export const useToggle = (initalValue: boolean) => {
    const [value, setValue] = useState<boolean>(initalValue);

    const toggleValue = () => setValue((prev: boolean) => !prev);

    return {value, toggleValue};
}