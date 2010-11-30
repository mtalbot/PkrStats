using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Moserware.Skills;

namespace ChessRank
{
	public class Pair<a, b>
	{
		public Pair(a left, b right)
		{
			Left = left;
			Right = right;
		}

		public a Left { get; set; }
		public b Right { get; set; }
	}
	class Program
	{


		static void Main(string[] args)
		{
			using (StreamReader reader = new StreamReader(args[0]))
			{
				reader.ReadLine();

				Dictionary<int, Pair<Player, Rating>> players = new Dictionary<int, Pair<Player, Rating>>();

				while (!reader.EndOfStream)
				{
					string[] line = reader.ReadLine().Split(',');

					int white = Convert.ToInt32(line[1]);
					int black = Convert.ToInt32(line[2]);

					if (!players.ContainsKey(white))
					{
						players.Add(white, new Pair<Player, Rating>(new Player(white), GameInfo.DefaultGameInfo.DefaultRating));
					}

					if (!players.ContainsKey(black))
					{
						players.Add(black, new Pair<Player, Rating>(new Player(black), GameInfo.DefaultGameInfo.DefaultRating));
					}

					int[] rank = new int[2];

					if (line[3] == "0")
					{
						rank = new int[] { 2, 1 };
					}
					else if (line[3] == "0")
					{
						rank = new int[] { 1, 2 };
					}
					else if (line[3] == "0.5")
					{
						rank = new int[] { 1, 1 };
					}

					IDictionary<Player, Rating> newscores = TrueSkillCalculator.CalculateNewRatings<Player>(GameInfo.DefaultGameInfo, new Dictionary<Player, Rating>[] { createDic(players[white]), createDic(players[black]) }, rank);

					foreach (var item in newscores)
					{
						players[(int)item.Key.Id].Right = item.Value;
					}
				}

				reader.Close();

				using (StreamReader reader2 = new StreamReader(args[1]))
				{
					using (StreamWriter writer = new StreamWriter(args[2]))
					{
						writer.WriteLine(reader2.ReadLine());

						while (!reader2.EndOfStream)
						{
							string[] line = reader2.ReadLine().Split(',');

							int white = Convert.ToInt32(line[1]);
							int black = Convert.ToInt32(line[2]);

							double prob = TrueSkillCalculator.CalculateMatchQuality<Player>(GameInfo.DefaultGameInfo, new Dictionary<Player, Rating>[] { createDic(players[white]), createDic(players[black]) });

							switch (players[white].Right.ConservativeRating.CompareTo(players[black].Right.ConservativeRating))
							{
								case -1:
									prob = prob / 2;
									break;
								case 0:
									prob = 0.5;
									break;
								case 1:
									prob = 1 - (prob / 2);
									break;
								default:
									prob = 0.5;
									break;
							}

							writer.WriteLine("{0},{1},{2},{3:0.00}", line[0], white, black, prob);
						}
		
						writer.Close();
					}
					reader2.Close();
				}
			}
		}

		static Dictionary<Player, Rating> createDic(Pair<Player, Rating> p)
		{
			Dictionary<Player, Rating> team = new Dictionary<Player, Rating>();
			team.Add(p.Left, p.Right);
			return team;
		}
	}
}
