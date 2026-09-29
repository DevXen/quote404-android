"""Chunk 28 batch 4: records 301-400 (ids 410379-410478). Appends to out/chunk_28.jsonl."""
import json, os

EN = {
410379: """@gundy: Every housewife should have a book about SQL secrets handy. You never know what your programmer's queries might be up to.""",
410380: """xxx: stereotypes are bad. I'm a girl, recently started working as a sysadmin.
"Inspectors" came to our office recently. They asked the director's secretary how to find the sysadmin. She explained. They asked her what he looks like. Well, she's new, I'm the first sysadmin she's ever seen. So she answered: "a sysadmin looks like a sysadmin..."
So the crew comes to me, I'm drinking tea. They look at me suspiciously. The next question made me choke on my tea:
"is it true that your sysadmin is a bearded guy in a sweater?"
xxx: I couldn't think of anything to answer. I just nodded at the plaque with my name and job title""",
410381: """xxx: What are you doing?
YYY: At this exact moment?
xxx: Yeah...
yyy: You know, I've got wifi in the toilet ;)""",
410382: """Боб: observing the most frequent use of the word "recessive" since biology lessons
Таша: well I do use strange words in my speech )
Таша: if only because the most beautiful cat I've ever seen is named Divergence of a Quadratic Function
Боб: how did that happen?
Таша: her owner graduated from Bauman
Таша: I'm allowed to call her Diva""",
410383: """xxx: Hello, why were servers 3 and 4 unavailable yesterday from 19:14 to 19:16?
yyy: quickly raised doesn't count as fallen.""",
410384: """from a conversation with a bassist friend
xxx: I met a girl, her last name is Bass )
yyy: does she play bass? x)
xxx: nah, only a bit of guitar.
xxx: but I'd marry her and take her last name!""",
410385: """Маша: Help, a question about your work
Маша: Basically a pipe 114 mm in diameter, 60mm wide, how many meters can we wrap into it if we have 234 square meters of material, accounting for 10 percent overlap.
Тони: What the hell is this?
Тони: Baba Nyura was slowly screwing screws into a ripe loaf.
Тони: How many nails will she need for the loaf to stay fresh?""",
410386: """лпш: following the reduction of time zones in Russia and the cancellation of the switch to winter time, Medvedev by his decree abolished midnight in Petropavlovsk-Kamchatsky.""",
410387: """Он: Now I have two free drawers in my nightstand.
Она: put something nice in them
Он: Don't even know what to put in there. I don't have a photo of you.
Она: well you can always ask for a photo
Он: I don't want a photo. I want you.
Она: don't put me in a drawer
Она: especially not in two""",
410388: """<Огонек> By the way, why in Russia do all magic tricks come with the words "abra kadabra", while in Harry Potter those words kill?
<Heavywave> what's good for a Russian is death for Potter""",
410389: """Akimura: Want me to visit you in a dream tonight? Only I'll be wearing a hat, and a green-and-red striped sweater.""",
410390: """xxx: As a kid I thought Fox Mulder was Valery Syutkin...""",
410391: """Л: don't you have a spare key for 7 lying around?
А: Only in the garage maybe
Л: For Windows 7, damn it!""",
410392: """xxx: Kostya do you love your job?
yyy: We love our job!
yyy: Labor is the road to prosperity
zzz: are you afraid the sysadmin is spying?
yyy: the sysadmin is not spying HE IS THE MOST WONDERFUL PERSON IN THE WORLD, WHOSE BEAUTY AND WIT ARE SURPASSED ONLY BY THE GODS OF OLYMPUS""",
410393: """Comments on an article:
xxx: and why, excuse me, would anyone need monitors bigger than 17 inches if they're not watching movies round the clock and editing photos? So they can move the mouse around more?
yyy: Why would someone who doesn't constantly walk around it need an apartment bigger than 10 sq.m.? So it takes longer to walk to the bathroom, right? :)""",
410394: """ххх: I see modernization and innovation have reached your backwater too
ххх: tying up tomatoes with twisted pair""",
410395: """ххх: How many points would you give mayor Sobyanin for his work?
ууу: 10 points, - answered yandex traffic jams""",
410396: """xxx: I'm sitting in the kitchen, decided to straighten the tablecloth the monitor stands on.
xxx: moved it.
xxx: a message popped up "network cable unplugged".
xxx: moved the tablecloth back - "connection speed 100mb/sec".
xxx: so what, my internet runs through the tablecloth?!""",
410397: """young_lipetsk: I sometimes connect via RDP client to a router, from there via RDP to a domain controller, from there via RDP to the required machine... So honestly, the plot of the movie "Inception" seemed banal and trite to me...""",
410398: """Nil: have you ever seen the face of a guy in the subway who, pretending to sleep, wanted to sneeze?""",
410399: """xxx: Ooooh god, I was so focused just now.
xxx: Lying in bed, laptop on my belly, a full mug of tea in my hand.
xxx: SUDDENLY realized I was about to sneeze...""",
410400: """xxx: yesterday I walked the dog, the neighbor for some reason decided to hang a carpet on the fence.
xxx: on one side of the fence the neighbor is pulling the carpet toward himself cursing, on the other side the dog is pulling toward itself growling.
xxx: weight of competitors: dog 70 kg, neighbor 75kg.
xxx: head-to-head score 0:1""",
410401: """After Japan assured that 11,500 tons of radioactive water being discharged into the open sea pose no threat to human health, the US government announced that the bombing of Libya is not only safe for the local population, but even lowers blood cholesterol and improves appetite""",
410402: """strah4: They ordered an office mascot for us.
strah4: A 6-meter python in a healthy terrarium.
strah4: They promise the mascot will now personally punish lateness and issue reprimands.""",
410403: """xxx: What are you doing?
ууу: Yeah, started a major cleanup
ххх: Whoa! What's gotten into you?
ууу: Yeah the mess got to me myself - can't find anything. Yesterday I brought a girl over. I clearly remember putting her on the bed. And in the morning I woke up - she's gone! So, putting things in order, looking for the girl. Her parents are probably worried already...""",
410404: """xxx: Nippon Steel Corporation won the Russian Railways order to supply 70 thousand tons of heat-treated rails in the first half of 2009. In 2008 Nippon Steel already supplied Russian Railways with 20 thousand tons of this product.
xxx: Japan, which has almost no natural resources, supplies rails to Russia. And you still say "selling snow to eskimos".""",
410405: """ххх: at the end of the month I'll go for an ultrasound and maybe find out there
yyy: are bets still open?
ххх: yep
ххх: a friend here told us - if it's a boy, he'll put up a case of champagne, and if we name him Denis, two more )
yyy: and if you name the girl Denis, then I'll put up three""",
410406: """A colleague (xxx) returned from a three-week sick leave.

xxx: In the hospital I was worried whether my sandwich had gone bad. I hope someone ate it?
yyy: Yes, I ate it.
xxx: Very good, you owe me 80 rubles.""",
410407: """xxx: Since %username% will be the only unmarried male at the wedding, the game "throw the garter to the males" will resemble dodgeball in the final round""",
410408: """Met a classmate, hadn't seen each other in 2 years, both had free time - went to a cafe to chat. Naturally for such a meeting, telling each other how life is going. I say I'm married. She:
- So who's your husband?
Obviously meaning the husband's profession - as one of the parameters of a successful marriage on the female scale. And my husband, I must say, administers Cisco hardware. Most of my social circle are IT people, so on autopilot I proudly answer:
- Admin-boobologist
The classmate-humanities-major, looking at my size four:
- Well, that you're a boobologist - that's obvious""",
410409: """Surmise: After 15 years of filling the dacha with stuff grandma finally admitted that the space there ran out.""",
410410: """From Habr:
Yeah that's right, my father has an anvil in his shed — my great-grandfather bought it 100 years ago. You wouldn't believe what they've forged on it over the century — still like new. But bend one or two nails on a laptop — and that's it, scratches.""",
410411: """xxx: Grandma brought her cell phone and a screwdriver.
xxx: Asked me to move the clock forward by an hour.""",
410412: """From a forum:

our potatoes are more expensive than pineapples and bananas
people are already asking whether you can fry pineapples.""",
410413: """katriona-cat:
No, all kinds of things happen, of course.
Like they forget to take paper tissues out of pockets before washing - and they get washed right in the pockets. Money gets washed. Documents. Keys. Notes. Badges-brooches-beads...
But to wash a piece of sausage together with the bathrobe - that takes our grandma!!!""",
410414: """k-k: It's good when there's some abstract bullshit drawn on the t-shirt
k-k: Drip some sauce and it's unnoticeable )""",
410415: """xxx: honey, you won't be late at work today, right?
yyy: shouldn't be, why?
xxx: just ) I'm waiting for you at home ) miss you so much )
xxx: and... buy cigarettes on the way, pls )""",
410416: """ххх: Was riding to the institute from my girlfriend's place in the morning. At night we were "experimenting" with her.
ххх: Well, and before the exam, from my backpack in front of the department head falls out...
ххх: A can of Contex Strong!
ууу: And you prepared well! :D
ххх: The department head told me the same thing.
ххх: But gave me the credit.""",
410417: """xxx: Life is a game of chess...
yyy: Jeez, how much of this pseudo-philosophical crap I've heard from everyone. Really.
yyy: Chess players say life is a game of chess.
yyy: Boxers and wrestlers - that life is a duel.
yyy: Salesmen - that life is all sales (of something, of yourself, of others).
yyy: Programmers - that life is a program.
yyy: Only the sewage workers, damn them, sit and modestly keep quiet.""",
410418: """Она: By the way, dad wants to get to know you better =)
Он: After he chased me around the district for 2 hours with an axe? ARE YOU KIDDING ME?!
Она: No, he liked you. He's a candidate master of sports in long-distance running.""",
410419: """xxx: the only thing I really liked about him was his cat...""",
410420: """xxx: standing outside smoking.
xxx: a guy runs past with a fire extinguisher in his hands, a huge column of powder erupting from it
xxx: and yelling "IT WORKS!!!\"""",
410421: """Alf_)): Exams are collecting signatures in support of candidates for engineers""",
410422: """Profugus: damn, shaved, now I've got nothing to scratch my hand on (""",
410423: """xxx: Misha, I'll probably head out soon, got things to do
yyy: Vova, what fucking things?
xxx: Well what kind of things do you think I could have?
yyy: What, did someone discover a batch of dicks that haven't been kicked hard enough?""",
410424: """ххх: Don't even know what to feed you, you're so picky.
ууу: I'm not picky, I'm a vegetarian.
ууу: you can feed me something plant-based...
ххх: Oil?""",
410425: """xxx: coming over?
yyy: no, I've got a major cleanup
xxx: whoa! What's gotten into you?
yyy: Well you know, invited a girl over the day before yesterday
yyy: And in the morning I wake up and see - she's talking to the mold in the fridge""",
410426: """ХХХ: I'm just trying to distract you
УУУ: well honey, you know, it's really hard to distract yourself from the thought that there's a chance that inside you, completely without your knowledge, desire or consent, some foreign life is trying to take root.
УУУ: and it's, fuck, not a colony of friendly bifidobacteria""",
410427: """Степашка: Call me.
Оля: you call
Степашка: No
Степашка: You're better at it.
Оля: of course, I'm better
Оля: but you call""",
410428: """Crevice: The wife took the kid to the hairdresser here. They shaved him almost bald. At five years old I would have raised a scandal over this. But he runs his hand over his head and asks - Dad, do I really look like a kiwi now?""",
410429: """ххх: Put a sausage in the microwave. For two minutes the sausage screamed with a human voice. I think I've gone gray.""",
410430: """xxx: Walking to the first class. Sleepy. Couldn't find my boots, had to put on slippery sneakers.
xxx: Slipped and crashed onto the ice right on my back.
xxx: First thought: "Damn, fuck me!".
xxx: Second thought: "Getting up is so haaard... five more minutes...\"""",
410431: """st-del: Yulya, I found info about these fucking infectious causes of abortions!
st-del: Especially delightful that the cause can be moldy feed...
st-del: And anyway, do you think info from the "promising pig farming" website will fly at the obstetrics exam?""",
410432: """xxx: I hate it when you click some unfamiliar button in a program, the program instantly hangs and starts meeeedlessly and ominously opening Internet Explorer.
xxx: It's like the Inquisition creates these buttons, so you'd know: you sinned - and you're fucked, the earth opens up and hello HELL""",
410433: """xxx:
I had a friend who was known in certain circles as a notable satanist, which by the way really helped with the ladies.

one underage silly girl kept pestering him with the question "how do I join the satanists"? in the end he told her that to do it she needs to, on the 13th, at full moon, in a cemetery, give herself to 13 full members of the sect. oh, and there should be a dead cat lying on her head.

the poor girl was silent for a minute, then asked in a thin, plaintive voice: "is there any way to do it WITHOUT THE DEAD CAT?\"""",
410434: """Harsh reality of a young female teacher: trading two 17-year-old admirers for one 34-year-old!""",
410435: """xxx: Forget it, the internet is big, by the time they ban me everywhere I'll have died of old age.""",
410436: """ххх: Ever since we had the little one, during sex I've trained myself to moan to the tune of "bayu-bayushki-bayu"...""",
410437: """Тыц-Пыц: 15 minutes ago the boss dropped by. Pinched his fingers in the door, farted, swore, turned red, apologized and left the office.
Тыц-Пыц: Sitting here racking our brains over the question - what did he actually come for?""",
410438: """A girl at the circus watches the lion performance with interest. Happily turns to mom: — Did you notice all the lions are boys?!
Mom, stunned by her daughter's biological knowledge, asks: — What makes you say that?
— Come on, mom! Forgot that lionesses don't wear manes?!""",
410439: """From lepra:
tapka: My home multimedia player in the TV at boot writes: "No storage for the present" I spent long hours contemplating this inscription and pondering its depth.""",
410440: """Штопор: > interesting, how does lithium turn into tritium in a reactor?
PSA: a neutron goes whack on the nucleus! It gets all overexcited and falls apart-crack! And an alpha particle flies out of it-pew!""",
410441: """xxx:
When girls see you for the first time, they usually say: Misha is so taciturn
I answer: well he's a programmer
They're surprised: wow, then he's just a chatterbox.""",
410442: """ххх: so what do you think of him? )
ууу: Well imagine: a dude walks around in a biker jacket, leather vest, leather hat, leather gloves, and cossack boots (also leather, though not sure) but wears jeans instead of leather pants.
ууу: So, he doesn't eat meat, because he FEELS SORRY FOR THE LITTLE ANIMALS!!!""",
410443: """ууу adds ххх as a friend

ххх: Hello. What do I owe?
ууу: nothing
ххх: Alright. Let's come at it from the other side. Why the fuck are you battering into my friends list?""",
410444: """xxx: best friends are when you arrive at a meeting half an hour late, and nobody's mad at you...
yyy: yeah, you arrive half an hour late, and nobody's even there yet!""",
410445: """Andrey Fedoseev: damn
Andrey Fedoseev: filling replacement
Andrey Fedoseev: at the dentist's under warranty
Andrey Fedoseev: how does that work?
Даня: 3 years or 10,000 kg of food""",
410446: """xxx: "Admin, 22 years old, white sweater."
xxx: Sounds, however, like a definition of mastery! Black belt, white sweater... Maybe admins should get differentiation by sweater color?!
yyy: Yeah, like, I have a white sweater in FreeBSD and a thick beard in Apache.
zzz: and I have a wrinkled turtleneck in twisted-pair pulling and three-day stubble in crimping""",
410447: """Сармель: my torrent app shows on a map where they're downloading from, there's a world map. and right now someone from the Pacific Ocean is downloading navitel from me.
Сармель: they got lost I guess :D""",
410448: """_Revan_: If man is born for happiness like a bird for flight, then in the world of birds I'd be a penguin.""",
410449: """xxx: Olya, never, hear me, NEVER! drink coffee at work in the morning. You won't fall asleep until lunch :)""",
410450: """ххх: once I pondered the harm and benefit of online games when the deputy director called me by my Lineage nickname...""",
410451: """From a forum:
ххх: Explain to me please in simple terms why the earth is round, and people and animals don't fall off? I really don't understand.
ууу: The gravity story, I take it, won't fly?""",
410452: """To the MLP torrent release:
xxx: Downloading for my daughter, thanks.
xxx: Although, who am I kidding... I don't have a daughter. I don't have anyone. Only ponies.""",
410453: """From the life of tech support.
operator: Internet tech support, hello!
subscriber: Hello, while I was waiting to get through to you - my kid crapped himself, goodbye!
Dial tone.""",
410454: """xxx: I'm watching a show about dung beetles. So interesting!
xxx: They're almost like people - they gather up crap, then drag it around with them their whole life.
xxx: And sometimes two or three times their own weight!""",
410455: """XXX: Help! My internet has become very small, everything's tiny! What should I do?
YYY: control and the mouse wheel""",
410456: """Winter. About five degrees below zero. Coming home from work. On the lawn between the entrances two bums are lying on their backs making some strange movements. About ten steps away stands a crowd of curious onlookers giggling. I raise my head: on the third floor a window is broken. I notice something dripping from the windowsill. I join the crowd to find out the details.
Turns out: a binge alcoholic on the third floor has no fridge, and he cools his vodka like this: puts the bottle in a bag, opens the window, places the bag with the bottle on the windowsill, closes the window pinching the bag's handles with it (so it doesn't fall).
These two comrades were throwing stones at the bag trying to knock it down. Couldn't knock it down, but managed to break the window and the bottle in the bag. And now vodka is dripping from the holes. And they, you know, are catching it with their mouths.""",
410457: """Today at the hairdresser's. A guy about 25 walks in, medium shagginess. Conversation:
- How much to get a haircut?
- 50 uah, but 10 more for washing your head, yours is really dirty.
The guy agrees, they wash his head, dry it, after which he looks in the mirror, yells "Oh! Just what I needed!" gives a ten and disappears. The hairdresser, seeing my bewilderment, explains "There's a dorm nearby..." Apparently this isn't rare for them )""",
410458: """xxx: And here's the split.
xxx: Putin considers the attacks on Libya outrageous and illegal, and Medvedev — the ones on LiveJournal.""",
410459: """Los - a friend from Norilsk.

los: finally buried the hamster
or-hor: was it that annoying?
los: sort of. in November the hamster died, well, decided to bury it in the morning, but for now - put it in a glass jar and carried it out to the balcony. husband got upset, went to smoke on the balcony and forgot to close the window after himself... basically, there was a snowfall in the evening, at night everything melted, and in the morning frost hit and everything froze solid. called repairmen, they said - the door needs to be broken down, and we just moved, money's at zero, decided to wait till spring. so spent the whole winter - with a view of the dead hamster from the window. guests didn't believe at first, thought we were joking
los: but the mother-in-law started visiting less often :)""",
410460: """eprst2000: I'm Alesya, but people very often mix up and in a hurry call me Oksana. Long used to it, calmly respond to Oksana and never correct. Other people call me Ksyusha. The logic is: Oksana is the same as Ksyusha. So I go for Ksyusha calmly too. Recently worked with Chinese, for them our names are worse than death. So they called me Sergey.""",
410461: """Conrad: The most patient and unflappable person I know is my boss. I can't even calmly talk about what he does. It makes me cringe, turns me inside out, I tried to imitate him, but didn't last 10 minutes. He's already elderly, a man of the old school, I understand everything, but... I can't imagine how it's possible, he ALREADY FOR 15 YEARS plays Lines for 6 hours every working day!!!!""",
410462: """xxx: LiveJournal learned to defend against hacker attacks.
xxx: At the slightest thing - it lies down and pretends to be dead.""",
410463: """Korvin: as soon as I walked into the exam, I went straight to take it
Korvin: first and without preparation
Korvin: for being first - +1 point
Korvin: for being unprepared - another +1
Korvin: passed with a 2""",
410464: """xxx: "On Kamchatka hunting geese and ducks that wintered in the Japanese prefecture of Fukushima is prohibited."
xxx: fucked up.
xxx: are they going to ask the ducks for their passports?""",
410465: """я: there's this company that deals with either paper or consumables, called "Belosnezhka", and apparently they have a phone number similar to ours. I love it when they call us, get to me and so tenderly ask: hello, is this Belosnezhka? I always get lost, don't know what to answer )
Гурына: Answer: No, this is duty gnome Marvin. What can I do for you?""",
410466: """YYY: I gained a kilo and a half
YYY: after 2 weeks of dumbbells
XXX: did you swallow one after all? )""",
410467: """Alex_Crazy: By the way, what do you have installed on your laptop now?
panika: a bag with stuff
Alex_Crazy: Wow. Stable? No glitches? )
panika: a year ago he spilled beer on it
panika: now only the bag with stuff managed to install stably""",
410468: """News: "Muscovites will be moved onto bicycles"

Гилл Бейтс:
I can picture it!
GONovtsy with flashers on their heads, on black lacquered BMX, and Medvedev with Putin on a yellow tandem! Beauty! Only who's going to steer?""",
410469: """xxx: bought special food for the cat so the cat wouldn't get fat.
xxx: The food really works, the cat doesn't eat it and thus doesn't get fat.""",
410470: """Дашуля..]: Where can I download a video card for my computer for free without SMS..??""",
410471: """@nekokoneko Ever since they removed the benches by the entrances, it's become unclear who's the prostitute in the building and who's the drug addict.""",
410472: """xxx: In Moscow Spartak and CSKA fans fought
xxx: That they were fans of these teams, they learned only the next day from the newspapers""",
410473: """Trikster:
the thing is, only imbeciles work in that regional dispensary. I need a certificate for the military enlistment office that I'm not registered there, and these dicks can't give me a certificate that I'm not registered with them, because... *drumroll* ... I'm NOT REGISTERED WITH THEM!!!!""",
410474: """xxx: I'm not fat at all, Sasha tells me I have a perfect figure.
yyy: Olya, he's a mathematician, for him a perfect figure is a SPHERE!""",
410475: """xxx:> so they also recently burned "Battle for Los Angeles" onto a flash drive for me and it turned out to be a battle with a Pornobanner""",
410476: """Was cleaning at home, found a notebook for writing down various useful things. Flipping through.
On the first page a definition of a database instance, system and program memory areas, on the next — what locks are, a retrospective query.
Turn the page — a borscht recipe.
Life goes on, priorities change. :)""",
410477: """About the dog:
- When Marta was little, that was her place. You'd give her the command "Place!" and she obeyed. Wherever she needed, that's where she'd drag her "place" to.""",
410478: """xxx: What's with your idiotic status?
yyy: normal status - collector of human emotions.
xxx: I had a neighbor in the dorm. He, when he came out of the shower, always put on shorts, and wrapped a towel over them.
xxx: He just, when a girl walked past him, would sharply start unwrapping the towel.
xxx: He really has a lot of emotions in his collection.""",
}

def main():
    src = '/home/hatch/workspace/translation-work/bashim/chunks/chunk_28.jsonl'
    outp = '/home/hatch/workspace/translation-work/bashim/out/chunk_28.jsonl'
    recs = {}
    with open(src, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line:
                r = json.loads(line)
                recs[r['id']] = r['text']
    assert len(recs) == 1000
    for i in EN:
        assert i in recs, i
    cur = 0
    if os.path.exists(outp):
        with open(outp, encoding='utf-8') as f:
            cur = sum(1 for line in f if line.strip())
    with open(outp, 'a', encoding='utf-8') as f:
        n = 0
        for i in sorted(EN):
            f.write(json.dumps({'id': i, 'ru': recs[i], 'en': EN[i]}, ensure_ascii=False) + '\n')
            n += 1
    print('batch28_4 OK, wrote', n, '(output now', cur + n, 'lines)')

if __name__ == '__main__':
    main()
