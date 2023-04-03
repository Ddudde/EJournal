import React, {useEffect, useRef} from "react";
import dialogCSS from './dialog.module.css';
import {dialog, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import start from "../../start/start.module.css";
import button from "../../button.module.css";
import {CHANGE_DIALOG_DEL, changeDialog} from "../../../store/actions";

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
    return dialogInfo.obj &&
        <div className={dialogCSS.over}>
            <div className={dialogCSS.block}>
                {dialogInfo.obj}
                {dialogInfo.buts && Object.getOwnPropertyNames(dialogInfo.buts).map(param =>
                    <div className={button.button+" "+start.button+" "+start.butz} onClick={() => dispatch(changeDialog(CHANGE_DIALOG_DEL))}>
                        {dialogInfo.buts[param].text}
                    </div>
                )}
            </div>
        </div>
}
export default Dialog;