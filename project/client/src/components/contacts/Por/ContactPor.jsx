import React, {useEffect, useReducer, useRef} from "react";
import {Helmet} from "react-helmet-async";
import contactCSS from '../contactMain.module.css';
import {contacts, states} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {chStatB, errorLoad, getEdCon, setActNew, setTyp} from "../ContactMain";
import ErrFound from "../../other/error/ErrFound";
import {CHANGE_EVENTS_CLEAR, changeEvents} from "../../../store/actions";

let dispatch, contactsInfo, type, errText, cState, inps;
type = "Por";
inps = {};
errText = "К сожалению, информация не найдена... Ждите новой информации.";
let [_, forceUpdate] = [];

export function ContactPor() {
    contactsInfo = useSelector(contacts);
    cState = useSelector(states);
    if(!dispatch) {
        setActNew(0);
        setTyp(type);
    }
    [_, forceUpdate] = useReducer((x) => x + 1, 0);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount ContactPor.jsx");
        setTyp(type);
        for(let el of document.querySelectorAll("." + contactCSS.ed + " > *[id^='inpn']")){
            chStatB({target: el}, inps);
        }
        return function() {
            dispatch(changeEvents(CHANGE_EVENTS_CLEAR));
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount ContactPor.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate ContactPor.jsx');
    });
    return (
        <div className={contactCSS.header}>
            <Helmet>
                <title>Контакты портала</title>
            </Helmet>
            {(!contactsInfo[type].contact && !contactsInfo[type].mapPr && !(cState.auth && cState.role == 4)) ?
                    <ErrFound text={errText}/>
                :
                    <div className={contactCSS.block}>
                        {(cState.auth && cState.role == 4) ?
                                getEdCon(contactsInfo, inps, forceUpdate)
                            :
                                <section className={contactCSS.center_colum}>
                                    <div className={contactCSS.blockTel}>
                                        <h1 className={contactCSS.zag}>Телефоны для связи</h1>
                                        <pre className={contactCSS.field}>
                                            {contactsInfo[type].contact}
                                        </pre>
                                    </div>
                                    <div className={contactCSS.map+" "+contactCSS.blockTel}>
                                        <h1 className={contactCSS.zag}>Карта проезда</h1>
                                        <pre className={contactCSS.field}>
                                            {contactsInfo[type].mapPr.text}
                                        </pre>
                                        <span className={contactCSS.banner}>
                                            <img alt="banner" src={contactsInfo[type].mapPr.imgUrl+''} onError={errorLoad}/>
                                        </span>
                                    </div>
                                </section>
                        }
                    </div>
            }
        </div>
    )
}
export default ContactPor;