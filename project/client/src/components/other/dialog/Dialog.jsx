import React, {useEffect, useRef} from "react";
import dialogCSS from './dialog.module.css';
import {dialog, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import button from "../../button.module.css";

export function Dialog() {
    const cState = useSelector(states);
    const dialogInfo = useSelector(dialog);
    const isFirstUpdate = useRef(true);
    const dispatch = useDispatch();
    useEffect(() => {
        console.log("I was triggered during componentDidMount Dialog.jsx");
        return function() {
            console.log("I was triggered during componentWillUnmount Dialog.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate ErrNotFound.jsx');
    });
    return dialogInfo.obj && <div className={dialogCSS.over}>
        <div className={dialogCSS.block}>
            {dialogInfo.obj}
            <div className={dialogCSS.blockBut}>
                {dialogInfo.buts && Object.getOwnPropertyNames(dialogInfo.buts).map((param, i, x, but = dialogInfo.buts[param]) =>
                    <div className={button.button+" "+dialogCSS.but} onClick={but.fun} data-enable={+but.enab} key={i}>
                        {but.text}
                    </div>
                )}
            </div>
        </div>
    </div>
}
export default Dialog;