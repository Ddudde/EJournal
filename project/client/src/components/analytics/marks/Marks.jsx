import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import marksCSS from './marks.module.css';
import {marks} from "../../../store/selector";
import {useDispatch, useSelector} from "react-redux";
import {setActNew} from "../AnalyticsMain";
import ErrFound from "../../other/error/ErrFound";

let dispatch, marksInfo, maxEl, errText;
errText = "К сожалению, информация не найдена... Можете попробовать попросить завуча заполнить информацию.";
maxEl = 0;

export function Marks() {
    marksInfo = useSelector(marks);
    for(let el of Object.getOwnPropertyNames(marksInfo.pers)){
        let len = Object.getOwnPropertyNames(marksInfo.pers[el].per).length;
        if(len > maxEl )maxEl = len;
    }
    if(!dispatch) setActNew(4);
    dispatch = useDispatch();
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount Marks.jsx");
        let scr = document.querySelector("." + marksCSS.predm);
        scr.scrollTo(scr.scrollWidth, 0);
        return function() {
            dispatch = undefined;
            console.log("I was triggered during componentWillUnmount Marks.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate Marks.jsx');
    });
    return (
        <div className={marksCSS.AppHeader}>
            <Helmet>
                <title>Итоговые оценки</title>
            </Helmet>
            {Object.getOwnPropertyNames(marksInfo.pers).length == 0 ?
                    <ErrFound text={errText}/>
                :
                    <div className={marksCSS.blockPredm}>
                        <div className={marksCSS.predm}>
                            <div className={marksCSS.persGrid} style={{gridTemplate: "15vh /22vw repeat(" + (maxEl + 2) + ", 2vw)"}}>
                                <div className={marksCSS.nav_i} id={marksCSS.nav_i}>
                                    <br/>
                                </div>
                                {marksInfo.namePers.map(param =>
                                    <div className={marksCSS.nav_i+" "+marksCSS.nav_iTextD} id={marksCSS.nav_i}>
                                        {param}
                                    </div>
                                )}
                                <div className={marksCSS.nav_i}>
                                    <div className={marksCSS.nav_iText}>
                                        Годовая
                                    </div>
                                </div>
                                <div className={marksCSS.nav_i}>
                                    <div className={marksCSS.nav_iText}>
                                        Итоговая
                                    </div>
                                </div>
                            </div>
                            {Object.getOwnPropertyNames(marksInfo.pers).map(param => <div className={marksCSS.predmGrid} style={{gridTemplate: "5vh /20vw repeat(" + (maxEl + 3) + ", 2vw)"}} id={param}>
                                    <div className={marksCSS.nav_i+" nam " + marksCSS.nam} id={marksCSS.nav_i}>
                                        {param}
                                    </div>
                                    <div className={marksCSS.nav_i+" "+marksCSS.nav_iBr} id={marksCSS.nav_i}>
                                        <br/>
                                    </div>
                                    <div className={marksCSS.nav_i+" "+marksCSS.nav_iBr} id={marksCSS.nav_i}>
                                        <br/>
                                    </div>
                                    {Object.getOwnPropertyNames(marksInfo.pers[param].per).map(param1 =>
                                        <div className={marksCSS.nav_i} id={marksCSS.nav_i}>
                                            {marksInfo.pers[param].per[param1]}
                                        </div>
                                    )}
                                    {Object.getOwnPropertyNames(marksInfo.pers[param].per).length < maxEl && Array(maxEl-Object.getOwnPropertyNames(marksInfo.pers[param].per).length).fill('').map(param =>
                                        <div className={marksCSS.nav_i} id={marksCSS.nav_i}>
                                            <br/>
                                        </div>
                                    )}
                                    <div className={marksCSS.nav_i + " " + marksCSS.nav_iTextM}>
                                        {marksInfo.pers[param].year}
                                    </div>
                                    <div className={marksCSS.nav_i + " " + marksCSS.nav_iTextM}>
                                        {marksInfo.pers[param].itog}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
            }
        </div>
    )
}
export default Marks;